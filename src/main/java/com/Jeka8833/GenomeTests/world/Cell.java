package com.Jeka8833.GenomeTests.world;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Cell implements Serializable {

    public final List<Layer> layers = new CopyOnWriteArrayList<>();
    public final World world;
    public final int x;
    public final int y;

    protected Cell(World world, int x, int y) {
        this.world = world;
        this.x = x;
        this.y = y;
    }

    public void tick() {
        for (Layer layer : layers) {
            layer.tick(this);
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getLayer(Class<T> type) {
        for (Layer layer : layers) {
            if (type.isInstance(layer)) {
                return (T) layer;
            }
        }
        return null;
    }

    public boolean containsLayer(Class<?> type) {
        for (Layer layer : layers) {
            if (type.isInstance(layer)) {
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
