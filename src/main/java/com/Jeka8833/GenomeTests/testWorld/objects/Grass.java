package com.Jeka8833.GenomeTests.testWorld.objects;

import com.Jeka8833.GenomeTests.world.Cell;
import com.Jeka8833.GenomeTests.world.CellLayers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Grass implements CellLayers {

    public static final int MAX_ENERGY = 20;
    public static final int START_ENERGY = MAX_ENERGY;
    private static final int PRECISION = 1_00;

    private final AtomicInteger energy = new AtomicInteger(START_ENERGY * PRECISION);

    @Override
    public void tick(Cell cell) {
    }

    public float takeEnergy() {
        return energy.getAndSet(0) / (float) PRECISION;
    }

    public void addEnergy(float value) {
        energy.accumulateAndGet((int) (value * PRECISION), (left, right) -> Math.min(MAX_ENERGY * PRECISION, left + right));
    }

    public void setEnergy(float value) {
        energy.set((int) (value * PRECISION));
    }

    public float getEnergy() {
        return energy.get() / (float) PRECISION;
    }

    public float getColorBrightness() {
        return 0.2f + (getEnergy() * 0.8f) / MAX_ENERGY;
    }

    public static void soilErosion(Cell... cells) {
        Grass[] grassList = new Grass[cells.length];
        int sum = 0;
        int count = 0;
        for (Cell cell : cells) {
            if (cell == null) continue;

            Grass grass = cell.getLayer(Grass.class);
            if(grass == null) return;

            sum += grass.energy.get();
            grassList[count++] = grass;
        }
        for (int i = 0; i < count; i++) {
            grassList[i].energy.set(sum / count);
        }
    }
}
