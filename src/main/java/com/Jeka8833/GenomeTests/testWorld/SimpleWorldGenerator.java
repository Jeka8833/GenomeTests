package com.Jeka8833.GenomeTests.testWorld;

import com.Jeka8833.GenomeTests.testWorld.objects.Grass;
import com.Jeka8833.GenomeTests.testWorld.objects.Seed;
import com.Jeka8833.GenomeTests.testWorld.objects.Sheet;
import com.Jeka8833.GenomeTests.testWorld.objects.Wood;
import com.Jeka8833.GenomeTests.world.Cell;
import com.Jeka8833.GenomeTests.world.CellLayers;
import com.Jeka8833.GenomeTests.world.World;
import com.Jeka8833.GenomeTests.world.WorldGenerator;

import java.util.Iterator;
import java.util.List;

public class SimpleWorldGenerator extends WorldGenerator {

    private static final int WORLD_WIDTH = 3000;
    private static final int WORLD_HEIGHT = 100;

    private static final int GROUND_LEVEL = 30;
    private static final int START_POPULATION = 1500;

    private World world;

    @Override
    public void create(World world) {
        this.world = world;
        for (int x = 0; x < WORLD_WIDTH; x++) {
            for (int y = 0; y < GROUND_LEVEL; y++) {
                Cell cell = world.getCell(x, y);
                if (cell != null) cell.layers.add(new Grass());
            }
        }

        for (int i = 0; i < START_POPULATION; i++) {
            int posX = (i * WORLD_WIDTH) / START_POPULATION;
            Cell cell = world.getCell(posX, GROUND_LEVEL - 1);
            if (cell != null) cell.layers.add(new Seed(TreeLive.newTree(null), 0));
        }
    }

    @Override
    public void preTick() {

    }

    @Override
    public void pastTick() {
        Iterator<TreeLive> treeLiveIterator = TreeLive.treeList.iterator();
        while (treeLiveIterator.hasNext()) {
            TreeLive tree = treeLiveIterator.next();
            if (!tree.isDead()) continue;

            tree.getTreeBlocks().removeIf(layers -> !(layers instanceof Seed));
            treeLiveIterator.remove();
        }

        Cell[] worldCells = world.getMap();
        int maxSunLevel = 16;
        for (int x = 0; x < WORLD_WIDTH; x++) {
            int level = maxSunLevel;
            for (int y = 0; y < GROUND_LEVEL; y++) {
                if (level <= 0) break;

                List<CellLayers> layers = worldCells[x + y * WORLD_WIDTH].layers;
                if (layers.isEmpty()) continue;

                for (CellLayers layer : layers) {
                    if (layer instanceof Wood) {
                        level -= 8;
                    } else if (layer instanceof Sheet sheet) {
                        sheet.treeLive.addHeath(level);
                        level -= 5;
                    }
                }
            }
        }
    }

    public static World createWorld() {
        var world = new World(WORLD_WIDTH, WORLD_HEIGHT, new SimpleWorldGenerator());
        world.setName("TestWorld1");
        world.setThreadCount(12);
        return world;
    }

}
