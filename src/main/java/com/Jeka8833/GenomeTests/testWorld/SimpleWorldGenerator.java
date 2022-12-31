package com.Jeka8833.GenomeTests.testWorld;

import com.Jeka8833.GenomeTests.testWorld.objects.Grass;
import com.Jeka8833.GenomeTests.testWorld.objects.Seed;
import com.Jeka8833.GenomeTests.testWorld.objects.Sheet;
import com.Jeka8833.GenomeTests.testWorld.objects.Wood;
import com.Jeka8833.GenomeTests.util.WorldManager;
import com.Jeka8833.GenomeTests.world.*;
import com.Jeka8833.GenomeTests.world.visualize.WindowManager;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

public class SimpleWorldGenerator extends WorldGenerator {

    private static final Random RANDOM = new Random();

    private static final int WORLD_WIDTH = 3000;
    private static final int WORLD_HEIGHT = 100;

    public static final int GROUND_LEVEL = 30;
    private static final int START_POPULATION = 3000;

    private World world;

    private final WorldManager worldManager;
    private transient final Genome[] bestGenomes;

    private int sameBlockTicks = 0;
    private int lastBlockCount = 0;
    private transient final ConcurrentSkipListSet<GenomeOrder> genomes = new ConcurrentSkipListSet<>(Comparator.comparingInt(GenomeOrder::tick));

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
                if (cell != null) cell.layers.add(new Grass());
            }
        }

        for (int i = 0; i < START_POPULATION; i++) {
            int posX = (i * WORLD_WIDTH) / START_POPULATION;
            Cell cell = world.getCell(posX, GROUND_LEVEL - 1);
            if (cell != null) {
                Genome genome;
                if (RANDOM.nextInt(101) <= 60 || bestGenomes == null || bestGenomes.length == 0) {
                    genome = Genome.createGenome(16);
                } else if (bestGenomes.length == 1) {
                    genome = bestGenomes[0];
                } else {
                    genome = bestGenomes[RANDOM.nextInt(bestGenomes.length)];
                }
                if (RANDOM.nextInt(101) <= 60) genome = genome.mutation(128);
                cell.layers.add(new Seed(genome, world));
            }
        }
    }

    @Override
    public void preTick() throws InterruptedException {
        int blocks = 0;
        boolean allDead = true;
        for (Cell cell : world.getMap()) {
            if (!cell.layers.isEmpty()) blocks++;
            if (allDead && (cell.containsLayer(Wood.class) || cell.containsLayer(Seed.class))) {
                allDead = false;
            }
        }
        if (lastBlockCount == blocks) {
            sameBlockTicks++;
        } else {
            sameBlockTicks = 0;
            lastBlockCount = blocks;
        }

        if (sameBlockTicks > 2000) {
            for (Cell cell : world.getMap()) {
                Wood wood = cell.getLayer(Wood.class);
                if (wood != null) wood.getTreeLive().addHeath(-100_000_000);
            }
        }

        if (allDead) {
            Thread.startVirtualThread(() -> {
                try {
                    if (genomes.isEmpty()) {
                        restart(worldManager, world, null);
                    } else {
                        System.out.println("Last genom on world " + world.getName() + ": " + genomes.last());
                        restart(worldManager, world,
                                genomes.stream()
                                        .skip(Math.max(0, genomes.size() - 2))
                                        .map(GenomeOrder::genome)
                                        .toArray(Genome[]::new));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            throw new InterruptedException();
        }
    }

    @Override
    public void endChunk(World world, int from, int to) {
        int maxSunLevel = getSunLevel(world.getTickCount());

        Cell[] worldCells = world.getMap();
        for (int x = from; x < to; x++) {
            int level = maxSunLevel;
            for (int y = WORLD_HEIGHT - 1; y >= GROUND_LEVEL; y--) {
                if (level <= 0) continue;

                for (Layer layer : worldCells[x + y * WORLD_WIDTH].layers) {
                    if (layer instanceof Wood) {
                        level -= 2;
                    } else if (layer instanceof Sheet sheet) {
                        if (level > 0) sheet.getTreeLive().addHeath(Math.min(7, level));

                        level -= 10;
                    } else if (layer instanceof Seed) {
                        level -= 1;
                    }
                }
            }

            if (level > 0) {
                Grass firstLayer = world.getCell(x, GROUND_LEVEL - 1).getLayer(Grass.class);
                if (firstLayer != null) firstLayer.addEnergy(level / 16f);
            }

            for (int y = 1; y < GROUND_LEVEL; y++) {
                Grass.soilErosion(world.getCell(x, y),
                        world.getShiftedCell(x, y, 1, 0),
                        world.getShiftedCell(x, y, 0, -1));
            }
        }
    }

    public static int getSunLevel(int ticks) {
        return 6 + (int) ((1 + Math.cos(2 * Math.PI * ticks / 500)) * 5);
    }

    @Override
    public void pastTick() {
    }

    public ConcurrentSkipListSet<GenomeOrder> getGenomes() {
        return genomes;
    }

    public static void restart(WorldManager worldManager, World world, Genome[] bestGenomes) throws InterruptedException {
        worldManager.getSimulation(world).stopAndWait();
        worldManager.remove(world);
        World newWorld = createWorld(worldManager,
                Integer.parseInt(world.getName().split("-")[1]) + 1, bestGenomes);
        WindowManager.getWindows().stream()
                .filter(window -> window.getWorld().equals(world))
                .forEach(window -> {
                    window.setWorld(newWorld);
                    window.setWindowTitle(newWorld.getName());
                });
        WorldSimulation simulation = worldManager.add(newWorld);
        simulation.start();
    }

    public static World createWorld(WorldManager worldManager, int number, Genome[] bestGenomes) {
        var world = new World(WORLD_WIDTH, WORLD_HEIGHT, new SimpleWorldGenerator(worldManager, bestGenomes));
        world.setName("TestWorld-" + number);
        world.setThreadCount(12);
        return world;
    }

    public record GenomeOrder(int tick, Genome genome) {
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
