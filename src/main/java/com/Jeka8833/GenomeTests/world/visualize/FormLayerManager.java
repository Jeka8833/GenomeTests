package com.Jeka8833.GenomeTests.world.visualize;

import com.Jeka8833.GenomeTests.world.Cell;

public interface FormLayerManager {

    void init(Window world);

    void preRender(Window world);

    void cellRender(Cell cell);

    void postRender(Window world);

    void close();

}
