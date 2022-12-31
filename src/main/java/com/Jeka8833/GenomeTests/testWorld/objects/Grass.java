package com.Jeka8833.GenomeTests.testWorld.objects;

import com.Jeka8833.GenomeTests.world.Cell;
import com.Jeka8833.GenomeTests.world.Layer;

public class Grass implements Layer {

    public static final int MAX_ENERGY = 20;
    public static final int START_ENERGY = MAX_ENERGY;

    private volatile float energy = START_ENERGY;

    @Override
    public void tick(Cell cell) {
    }

    public float takeEnergy() {
        float value = energy;
        energy = 0;
        return value;
    }

    public void addEnergy(float value) {
        energy = Math.min(MAX_ENERGY, energy + value);
    }

    public void setEnergy(float value) {
        energy = value;
    }

    public float getEnergy() {
        return energy;
    }

    public float getColorBrightness() {
        return 0.2f + (energy * 0.8f) / MAX_ENERGY;
    }

    public static void soilErosion(Cell... cells) {
        Grass[] grassList = new Grass[cells.length];
        float sum = 0;
        int count = 0;
        for (Cell cell : cells) {
            if (cell == null) continue;

            Grass grass = cell.getLayer(Grass.class);
            if (grass == null) continue;

            sum += grass.energy;
            grassList[count++] = grass;
        }
        for (int i = 0; i < count; i++) {
            grassList[i].energy = sum / count;
        }
    }
}
