package com.Jeka8833.GenomeTests.world;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Cell implements Serializable {

    public final List<CellLayers> layers = new CopyOnWriteArrayList<>();

    public final World world;
    public final int x;
    public final int y;

    protected Cell(World world, int x, int y) {
        this.world = world;
        this.x = x;
        this.y = y;
    }

    public void tick() {
        for (CellLayers cloneUnmodifiableLayer : layers) {
            cloneUnmodifiableLayer.tick(this);
        }
    }

    public <T> T getLayer(Class<T> type) {
        for (CellLayers cellLayers : layers) {
            if (type.isInstance(cellLayers)) {
                return type.cast(cellLayers);
            }
        }
        return null;
    }
}
