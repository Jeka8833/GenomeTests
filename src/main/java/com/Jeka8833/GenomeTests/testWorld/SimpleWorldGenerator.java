package com.Jeka8833.GenomeTests.testWorld;

import com.Jeka8833.GenomeTests.testWorld.objects.Grass;
import com.Jeka8833.GenomeTests.testWorld.objects.Seed;
import com.Jeka8833.GenomeTests.testWorld.objects.Sheet;
import com.Jeka8833.GenomeTests.testWorld.objects.Wood;
import com.Jeka8833.GenomeTests.world.*;
import com.Jeka8833.GenomeTests.world.visualize.WorldFrame;

import java.util.List;

public class SimpleWorldGenerator extends WorldGenerator {

    private static final int WORLD_WIDTH = 3000;
    private static final int WORLD_HEIGHT = 100;

    public static final int GROUND_LEVEL = 30;
    private static final int START_POPULATION = 100;

    private World world;

    private final WorldTimeManager timeManager;

    public SimpleWorldGenerator(WorldTimeManager timeManager) {
        this.timeManager = timeManager;
    }

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
        boolean allDead = true;
        Cell[] worldCells = world.getMap();
        for (Cell cell : worldCells) {
            if (cell.getLayer(Wood.class) != null || cell.getLayer(Seed.class) != null) {
                allDead = false;
                break;
            }
        }
        if (allDead) {
            restart(timeManager, world);
        }
    }

    @Override
    public void pastTick() {
        int maxSunLevel = 25;

        Cell[] worldCells = world.getMap();
        for (int x = 0; x < WORLD_WIDTH; x++) {
            int level = maxSunLevel;
            for (int y = 0; y < WORLD_HEIGHT; y++) {
                List<CellLayers> layers = worldCells[x + y * WORLD_WIDTH].layers;
                int size = layers.size();
                if (size == 0) continue;

                for (int i = size - 1; i >= 0; i--) {
                    CellLayers cellLayers = layers.get(i);
                    if (cellLayers instanceof Wood wood) {
                        if (!wood.getTreeLive().isDead()) {
                            level -= 8;
                        }
                    } else if (cellLayers instanceof Sheet sheet) {
                        if (!sheet.getTreeLive().isDead()) {
                            if (level > 0) sheet.getTreeLive().addHeath(Math.min(40, level));
                            level -= 5;
                        }
                    } else if (cellLayers instanceof Seed sheet) {
                        if (!sheet.getTreeLive().isDead()) {
                            level -= 3;
                        }
                    }
                }
            }
        }
    }

    private static WorldFrame window = null;
    private static Thread restartThread = null;

    public static void restart(WorldTimeManager timeManager, World world) {
        if (restartThread == null || !restartThread.isAlive()) {
            restartThread = new Thread(() -> {
                timeManager.stopAndAwait();
                SimpleWorldGenerator.close(timeManager, world);
                SimpleWorldGenerator.createWorld(timeManager,
                        Integer.parseInt(world.getName().split("-")[1]) + 1);
                timeManager.start();
            });
            restartThread.setDaemon(true);
            restartThread.setPriority(Thread.MIN_PRIORITY);
            restartThread.start();
        }
    }

    public static void createWorld(WorldTimeManager timeManager, int number) {
        var world = new World(WORLD_WIDTH, WORLD_HEIGHT, new SimpleWorldGenerator(timeManager));
        world.setName("TestWorld-" + number);
        world.setThreadCount(4);
        world.setLimitTickPerMinute(0);
        window = WorldFrame.createWindow(world, new FrameManager());
        timeManager.addWorld(world);
    }

    public static void close(WorldTimeManager timeManager, World world) {
        timeManager.removeWorld(world.getName());
        if (window != null)
            window.destroyFrame();
    }
}
