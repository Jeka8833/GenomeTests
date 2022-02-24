package com.Jeka8833.GenomeTests.world;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Cell implements Serializable {

    public final List<CellLayers> layers = Collections.synchronizedList(new ArrayList<>());

    public final World world;
    public final int x;
    public final int y;

    protected Cell(World world, int x, int y) {
        this.world = world;
        this.x = x;
        this.y = y;
    }

    public void tick() {
        for (int i = layers.size() - 1; i >= 0; i--) {
            layers.get(i).tick(this);
        }
    }

    public <T> T getLayer(Class<T> type) {
        synchronized (layers) {
            for (CellLayers cellLayers : layers) {
                if (type.isInstance(cellLayers)) {
                    return type.cast(cellLayers);
                }
            }
        }
        return null;
    }
}
