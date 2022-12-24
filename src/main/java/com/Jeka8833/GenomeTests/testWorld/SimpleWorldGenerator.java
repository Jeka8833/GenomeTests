package com.Jeka8833.GenomeTests.testWorld;

import com.Jeka8833.GenomeTests.testWorld.objects.Grass;
import com.Jeka8833.GenomeTests.testWorld.objects.Seed;
import com.Jeka8833.GenomeTests.testWorld.objects.Sheet;
import com.Jeka8833.GenomeTests.testWorld.objects.Wood;
import com.Jeka8833.GenomeTests.util.WorldManager;
import com.Jeka8833.GenomeTests.world.*;
import com.Jeka8833.GenomeTests.world.visualize.WindowManager;

import java.util.*;

public class SimpleWorldGenerator extends WorldGenerator {

    private static final int WORLD_WIDTH = 3000;
    private static final int WORLD_HEIGHT = 100;

    public static final int GROUND_LEVEL = 30;
    private static final int START_POPULATION = 500;

    private World world;

    private final WorldManager worldManager;
    private final Genome[] bestGenomes;

    private final SortedSet<GenomeOrder> genomes = new TreeSet<>(Comparator.comparingInt(GenomeOrder::tick));

    public SimpleWorldGenerator(WorldManager worldManager, Genome[] bestGenomes) {
        this.worldManager = worldManager;
        this.bestGenomes = bestGenomes;
    }

    @Override
    public void create(World world) {
        this.world = world;
        for (int x = 0; x < WORLD_WIDTH; x++) {
            for (int y = 0; y < GROUND_LEVEL; y++) {
                Cell cell = world.getCell(x, y);
                cell.layers.add(new Grass());
            }
        }

        Random random = new Random();
/*
        world.getCell(50, 50).layers.add(new Seed(TreeLive.newTree(null), random.nextInt(16)));
        world.getCell(50, 48).layers.add(new Wood(TreeLive.newTree(null), random.nextInt(16)));
        world.getCell(50, 47).layers.add(new Wood(TreeLive.newTree(null), random.nextInt(16)));
        world.getCell(50, 46).layers.add(new Wood(TreeLive.newTree(null), random.nextInt(16)));
        world.getCell(50, 45).layers.add(new Wood(TreeLive.newTree(null), random.nextInt(16)));
        world.getCell(50, 44).layers.add(new Wood(TreeLive.newTree(null), random.nextInt(16)));
*/

        for (int i = 0; i < START_POPULATION; i++) {
            int posX = (i * WORLD_WIDTH) / START_POPULATION;
            Cell cell = world.getCell(posX, GROUND_LEVEL - 1);
            if (cell != null) {
                Genome genome;
                if (random.nextInt(101) <= 70 || bestGenomes == null || bestGenomes.length == 0) {
                    genome = null;
                } else if (bestGenomes.length == 1) {
                    genome = bestGenomes[0];
                } else {
                    genome = bestGenomes[random.nextInt(bestGenomes.length)];
                }
                cell.layers.add(new Seed(TreeLive.newTree(genome == null ? null : new TreeLive(genome)), random.nextInt(16)));
            }
        }
    }

    @Override
    public void preTick() {
        boolean allDead = true;
        for (Cell cell : world.getMap()) {
            if (cell.getLayer(Wood.class) != null || cell.getLayer(Seed.class) != null) {
                allDead = false;
                break;
            }
        }
        if (allDead) {
            System.out.println("Last: " + genomes.last());
            restart(worldManager, world,
                    genomes.stream()
                            .skip(genomes.size() - 2)
                            .map(GenomeOrder::genome)
                            .toArray(Genome[]::new));
        }
    }

    @Override
    public void pastTick() {
        int maxSunLevel = 16;

        Cell[] worldCells = world.getMap();
        for (int x = 0; x < WORLD_WIDTH; x++) {
            int level = maxSunLevel;
            for (int y = GROUND_LEVEL; y < WORLD_HEIGHT; y++) {
                for (CellLayers layer : worldCells[x + y * WORLD_WIDTH].layers) {
                    if (layer instanceof Wood wood) {
                        level -= 3;

                        if (wood.getTreeLive().isDead())
                            genomes.add(new GenomeOrder(world.getTickCount(), wood.getTreeLive().getGenome()));
                    } else if (layer instanceof Sheet sheet) {
                        if (level > 0) sheet.getTreeLive().addHeath(Math.min(7, level));
                        level -= 10;

                        if (sheet.getTreeLive().isDead())
                            genomes.add(new GenomeOrder(world.getTickCount(), sheet.getTreeLive().getGenome()));
                    } else if (layer instanceof Seed seed) {
                        level -= 1;

                        if (seed.getTreeLive().isDead())
                            genomes.add(new GenomeOrder(world.getTickCount(), seed.getTreeLive().getGenome()));
                    }
                }
            }
        }
    }

    public SortedSet<GenomeOrder> getGenomes() {
        return genomes;
    }

    public static void restart(WorldManager worldManager, World world, Genome[] bestGenomes) {
        worldManager.getSimulation(world).stop();
        worldManager.remove(world);
        World newWorld = createWorld(worldManager,
                Integer.parseInt(world.getName().split("-")[1]) + 1, bestGenomes);
        WindowManager.getWindows().stream()
                .filter(window -> window.getWorld().equals(world))
                .forEach(window -> window.setWorld(newWorld));
        WorldSimulation simulation = worldManager.add(newWorld);
        simulation.start();
    }

    public static World createWorld(WorldManager worldManager, int number, Genome[] bestGenomes) {
        var world = new World(WORLD_WIDTH, WORLD_HEIGHT, new SimpleWorldGenerator(worldManager, bestGenomes));
        world.setName("TestWorld-" + number);
        world.setThreadCount(11);
        return world;
    }

    private record GenomeOrder(int tick, Genome genome) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            GenomeOrder that = (GenomeOrder) o;

            return Objects.equals(genome, that.genome);
        }

        @Override
        public int hashCode() {
            return genome != null ? genome.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "GenomeOrder{" +
                    "tick=" + tick +
                    ", genome=" + genome +
                    '}';
        }
    }
}
