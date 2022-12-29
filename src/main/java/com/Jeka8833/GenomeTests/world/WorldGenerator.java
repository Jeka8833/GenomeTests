package com.Jeka8833.GenomeTests.world;

import java.io.Serializable;

public abstract class WorldGenerator implements Serializable {
    public abstract void create(World world);

    public abstract void preTick() throws InterruptedException;

    public abstract void endChunk(World world, int from, int to);

    public abstract void pastTick();
}