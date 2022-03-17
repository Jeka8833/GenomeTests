package com.Jeka8833.GenomeTests.world;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class World implements Serializable {
    private static final Logger LOGGER = LogManager.getLogger(World.class);
    private static final int timeForSamples = 30_000;

    private transient ExecutorService threads;
    private int threadCount = 1;

    private transient long timeToNextTick = 0;
    private transient int countTickPerMinute = Integer.MIN_VALUE;
    private transient int lastTickCount = 0;
    private int tickCount = 0;

    private String name = UUID.randomUUID().toString();
    private final int width;
    private final int height;
    private final Cell[] map;
    private final WorldGenerator generator;

    private boolean skipSimulation = false;
    private int limitTickPerMinute = 0; // <= 0 is disable

    public World(int width, int height, WorldGenerator generator) {
        if (width < 1 || height < 1) throw new IllegalArgumentException("Width or height < 0");

        this.width = width;
        this.height = height;
        this.generator = generator;

        int mapSize = width * height;
        map = new Cell[mapSize];
        for (int i = 0; i < mapSize; i++)
            map[i] = new Cell(this, i % width, i / width);

        generator.create(this);
    }

    public void tick() throws InterruptedException {
        if (skipSimulation) return;
        long time = System.currentTimeMillis();
        if (limitTickPerMinute > 0) {
            long delayTime = (((tickCount - lastTickCount + 1) * 60_000L) / limitTickPerMinute) -
                    (timeForSamples - (timeToNextTick - time));
            if (delayTime > 0) Thread.sleep(delayTime);
        }
        if (time > timeToNextTick) {
            countTickPerMinute =
                    (int) ((60_000 * (tickCount - lastTickCount)) / (timeForSamples + (time - timeToNextTick)));
            timeToNextTick = time + timeForSamples;
            lastTickCount = tickCount;
        }

        generator.preTick();

        if (threads == null) {
            new WorldRunnable(this, 0, map.length, null).run();
        } else {
            var lock = new CountDownLatch(threadCount);
            for (int i = 0; i < threadCount; i++) {
                int fromPos = (i * map.length) / threadCount;
                int toPos = ((i + 1) * map.length) / threadCount;
                var worldWorker = new WorldRunnable(this, fromPos, toPos, lock);

                if (i >= threadCount - 1) // Check last iteration
                    worldWorker.run();
                else threads.execute(worldWorker);
            }

            lock.await(); // Wait to the end working all threads
        }

        generator.pastTick();
        tickCount++;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        if (threadCount < 1) throw new IllegalArgumentException("Min thread count is 1");

        threads = threadCount == 1 ? null : Executors.newFixedThreadPool(threadCount - 1);
        this.threadCount = threadCount;
    }

    public int getAvgTickPerMinute() {
        return countTickPerMinute;
    }

    public int getTickCount() {
        return tickCount;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Cell[] getMap() {
        return map;
    }

    public WorldGenerator getGenerator() {
        return generator;
    }

    public boolean isSkipSimulation() {
        return skipSimulation;
    }

    public void setSkipSimulation(boolean skipSimulation) {
        this.skipSimulation = skipSimulation;
    }

    public int getLimitTickPerMinute() {
        return limitTickPerMinute;
    }

    public void setLimitTickPerMinute(int limitTickPerMinute) {
        this.limitTickPerMinute = limitTickPerMinute;
    }

    public Cell getCell(int x, int y) {
        int index = x + y * width;
        if (index < 0 || index >= map.length) return null;
        return map[index];
    }

    public Cell getShiftedCell(int x, int y, int shiftX, int shiftY) {
        x = (x + shiftX) % width;
        if (x < 0) x += width;

        return getCell(x, y + shiftY);
    }

    private record WorldRunnable(World world, int from, int to, CountDownLatch lock) implements Runnable {
        @Override
        public void run() {
            try {
                for (int i = from; i < to; i++)
                    world.map[i].tick();
            } catch (Exception e) {
                LOGGER.error("Block from: " + from + " to " + to + " has error:", e);
            }
            if (lock != null) lock.countDown();
        }
    }
}
