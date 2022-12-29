package com.Jeka8833.GenomeTests.world.visualize;

import com.Jeka8833.GenomeTests.world.Cell;

public interface FormLayer {

    void init(Window window);

    void preRender(Window window);

    void cellRender(Window window, Cell cell);

    void postRender(Window window);

    void close(Window window);

}
