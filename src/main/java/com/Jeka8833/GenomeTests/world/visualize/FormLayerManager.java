package com.Jeka8833.GenomeTests.world.visualize;

import com.Jeka8833.GenomeTests.world.Cell;

public interface FormLayerManager {

    void init(WorldFrame world);

    void preRender(WorldFrame world);

    void cellRender(Cell cell);

    void postRender(WorldFrame world);

    void close();

}
