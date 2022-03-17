package com.Jeka8833.GenomeTests.world;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorldTimeManager implements Serializable {
    private static final Logger LOGGER = LogManager.getLogger(WorldTimeManager.class);
    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

    private final List<World> worlds = new ArrayList<>();
    private transient CyclicBarrier lockThreads;

    private transient volatile boolean run = false;
    private volatile boolean tickSynchronization = false;

    public void start() {
        if (run || worlds.isEmpty()) return;
        run = true;

        lockThreads = new CyclicBarrier(worlds.size());
        for (final World world : worlds) {
            THREAD_POOL.execute(() -> {
                while (run) {
                    try {
                        world.tick();
                    } catch (InterruptedException e) {
                        LOGGER.warn("Fail run tick");
                    }
                    if (tickSynchronization) {
                        try {
                            lockThreads.await();
                        } catch (InterruptedException | BrokenBarrierException ignored) {
                            // Ignore
                        }
                    }
                }
            });
        }
    }

    public void stop() {
        run = false;
    }

    public boolean isRun() {
        return run;
    }

    public boolean isTickSynchronization() {
        return tickSynchronization;
    }

    public void setTickSynchronization(boolean tickSynchronization) {
        this.tickSynchronization = tickSynchronization;
        if (!tickSynchronization && lockThreads != null) lockThreads.reset();
    }

    public List<World> getWorlds() {
        return worlds;
    }

    public World getWorld(String name) {
        for (World world : worlds)
            if (world.getName().equalsIgnoreCase(name))
                return world;
        return null;
    }

    public void addWorld(World world) {
        worlds.add(world);
    }
}
