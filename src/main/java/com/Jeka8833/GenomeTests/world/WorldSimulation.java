package com.Jeka8833.GenomeTests.world;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.time.Duration;

public class WorldSimulation implements Serializable {

    private static final Logger LOGGER = LogManager.getLogger(WorldSimulation.class);

    private static final int rateMeterTimeSample = 30_000;

    public static final int WORLD_STOPPED = 0;
    public static final int WORLD_STARTED = 1;
    public static final int WORLD_TRYING_STOP = 2;
    public static final int WORLD_FREEZE = 3;


    private final @NotNull World world;

    private transient @Nullable Thread thread;
    private @Nullable Bucket rateLimitBucket;
    private int limitTickPerMinute = Integer.MIN_VALUE;

    private transient long reteMeterTime = 0;
    private transient int ticksPerMinute = 0;
    private transient int lastTicks = 0;

    private @Nullable SynchronizeSimulation synchronizer;
    private transient volatile int worldStatus = WORLD_STOPPED;

    public WorldSimulation(World world) {
        this(world, null);
    }

    public WorldSimulation(World world, SynchronizeSimulation synchronizer) {
        if (world == null) throw new IllegalArgumentException("World is null");

        this.world = world;
        this.synchronizer = synchronizer;
    }

    public void start() {
        if (isRun()) return;

        thread = Thread.startVirtualThread(() -> {
            try {
                if (synchronizer != null) synchronizer.registerWorld(this);
                while (worldStatus == WORLD_STARTED) {
                    try {
                        if (System.currentTimeMillis() > reteMeterTime) {
                            ticksPerMinute = (int) ((60_000 * (world.getTickCount() - lastTicks)) /
                                    (System.currentTimeMillis() - reteMeterTime + rateMeterTimeSample));
                            lastTicks = world.getTickCount();
                            reteMeterTime = System.currentTimeMillis() + rateMeterTimeSample;
                        }

                        world.tick();

                        if (synchronizer != null) {
                            synchronizer.tick();
                        } else if (rateLimitBucket != null) {
                            rateLimitBucket.asBlocking().consume(1);
                        }
                    } catch (InterruptedException interruptedException) {
                        break; // Canceling loop
                    } catch (Exception ex) {
                        LOGGER.warn("An error has occurred during the tick.", ex);
                    }
                }
            } finally {
                worldStatus = WORLD_STOPPED;
                if (synchronizer != null) synchronizer.unregisterWorld(this);
            }
        });
        worldStatus = WORLD_STARTED;
    }

    public void stop() {
        if (thread != null) thread.interrupt();
        if (worldStatus == WORLD_STARTED) worldStatus = WORLD_TRYING_STOP;
    }

    @Blocking
    public boolean stopAndWait() {
        stop();
        boolean isStop = waitingEnd(60_000);
        if (isStop) return true;

        worldStatus = WORLD_FREEZE;
        return false;
    }

    public boolean isRun() {
        return worldStatus == WORLD_STARTED || worldStatus == WORLD_TRYING_STOP;
    }

    @NotNull
    public World getWorld() {
        return world;
    }

    @MagicConstant(flags = {WORLD_STOPPED, WORLD_STARTED, WORLD_TRYING_STOP, WORLD_FREEZE})
    public int getWorldStatus() {
        return worldStatus;
    }

    @Nullable
    public Bucket getRateLimit() {
        return rateLimitBucket;
    }

    public int getLimitTickPerMinute() {
        return limitTickPerMinute;
    }

    public int getTicksPerMinute() {
        return ticksPerMinute;
    }

    public void setSpeed(int tickPerMinute) {
        if (tickPerMinute <= 0) {
            limitTickPerMinute = Integer.MIN_VALUE;
            rateLimitBucket = null;
        } else {
            this.limitTickPerMinute = tickPerMinute;
            rateLimitBucket = Bucket.builder()
                    .addLimit(Bandwidth.simple(tickPerMinute, Duration.ofMinutes(1)).withInitialTokens(0))
                    .build();
        }
        if (synchronizer != null) synchronizer.recalculateSynchronize();
    }

    public void setSynchronizer(@Nullable SynchronizeSimulation synchronizer) {
        this.synchronizer = synchronizer;
    }

    @Blocking
    public void waitEnd() {
        waitingEnd(0);
    }

    @Blocking
    public boolean waitingEnd(long timeout) {
        if (timeout < 0) throw new IllegalArgumentException("Invalid value 'time'");
        if (thread == null) return true;

        long startTime = System.currentTimeMillis();
        while (true) {
            try {
                if (timeout == 0) {
                    thread.join();
                    return true;
                }

                return thread.join(Duration.ofMillis(timeout));
            } catch (IllegalThreadStateException e) {
                return true;
            } catch (InterruptedException e) {
                if (timeout != 0) {
                    timeout -= System.currentTimeMillis() - startTime;
                    if (timeout <= 0) return false;
                }
            }
        }
    }
}
