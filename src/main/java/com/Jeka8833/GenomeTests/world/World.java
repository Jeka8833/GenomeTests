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
    public static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();
    private int threadCount = 1;

    private int tickCount = 0;

    private String name = UUID.randomUUID().toString();
    private final int width;
    private final int height;
    private final Cell[] map;
    private final WorldGenerator generator;

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
        generator.preTick();
/*
        if (threadCount <= 1) {
            new WorldUpdateLayers(this, 0, map.length, null).run();
        } else {
            var lock = new CountDownLatch(threadCount);
            for (int i = 0; i < threadCount; i++) {
                int fromPos = (i * map.length) / threadCount;
                int toPos = ((i + 1) * map.length) / threadCount;
                var worldWorker = new WorldUpdateLayers(this, fromPos, toPos, lock);

                if (i >= threadCount - 1) // Check last iteration
                    worldWorker.run();
                else THREAD_POOL.execute(worldWorker);
            }

            lock.await(); // Wait to the end working all threads
        }
*/
        if (threadCount <= 1) {
            new WorldRunnable(this, 0, map.length, null).run();
        } else {
            var lock = new CountDownLatch(threadCount);
            for (int i = 0; i < threadCount; i++) {
                int fromPos = (i * map.length) / threadCount;
                int toPos = ((i + 1) * map.length) / threadCount;
                var worldWorker = new WorldRunnable(this, fromPos, toPos, lock);

                if (i >= threadCount - 1) // Check last iteration
                    worldWorker.run();
                else THREAD_POOL.execute(worldWorker);
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
        this.threadCount = threadCount;
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
                for (int i = from; i < to; i++) world.map[i].tick();
            } catch (Exception e) {
                LOGGER.error("Block from: " + from + " to " + to + " has error:", e);
            }
            if (lock != null) lock.countDown();
        }
    }
/*
    private record WorldUpdateLayers(World world, int from, int to, CountDownLatch lock) implements Runnable {
        @Override
        public void run() {
            for (int i = from; i < to; i++) world.map[i].updateLayers();

            if (lock != null) lock.countDown();
        }
    }*/
}
