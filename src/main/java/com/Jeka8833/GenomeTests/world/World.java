package com.Jeka8833.GenomeTests.world;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.UUID;

public class World implements Serializable {
    private static final Logger LOGGER = LogManager.getLogger(World.class);
    private final ChunkParallelization<World> chunkManager;

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

        chunkManager = new ChunkParallelization<>(0, width, this, (ChunkParallelization.Chunk<World>) (world, from, to) -> {
            int start = from * height;
            int end = to * height;

            try {
                for (int i = start; i < end; i++) world.map[i].tick();

                generator.endChunk(world, from, to);
            } catch (Exception e) {
                LOGGER.error("X chunk from: " + from + " to " + to + " has error:", e);
            }
        });
    }

    public void tick() throws InterruptedException {
        generator.preTick();

        chunkManager.start();

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
        return chunkManager.getThreads();
    }

    public void setThreadCount(int threadCount) {
        chunkManager.setThreads(threadCount);
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

    @Nullable
    public Cell getCell(int x, int y) {
        int index = x + y * width;
        if (index < 0 || index >= map.length) return null;
        return map[index];
    }

    @Nullable
    public Cell getShiftedCell(int x, int y, int shiftX, int shiftY) {
        x = (x + shiftX) % width;
        if (x < 0) x += width;

        return getCell(x, y + shiftY);
    }
}
