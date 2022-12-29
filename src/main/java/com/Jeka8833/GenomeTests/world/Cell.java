package com.Jeka8833.GenomeTests.world;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Cell implements Serializable {

    public final List<CellLayers> layers = new CopyOnWriteArrayList<>();
    //public final Set<CellLayers> layers = Collections.newSetFromMap(new ConcurrentHashMap<>());
    public final World world;
    public final int x;
    public final int y;

    protected Cell(World world, int x, int y) {
        this.world = world;
        this.x = x;
        this.y = y;
    }

    public void tick() {
        for (CellLayers layer : layers) {
            layer.tick(this);
        }
    }

    @Nullable
    public <T> T getLayer(Class<T> type) {
        for (CellLayers cellLayers : layers) {
            if (type.isInstance(cellLayers)) {
                return type.cast(cellLayers);
            }
        }
        return null;
    }

    public boolean containsLayer(Class<?> type) {
        for (CellLayers cellLayers : layers) {
            if (type.isInstance(cellLayers)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Cell{" +
                "layers=" + layers +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}
