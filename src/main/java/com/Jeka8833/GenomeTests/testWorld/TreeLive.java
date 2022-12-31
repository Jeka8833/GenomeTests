package com.Jeka8833.GenomeTests.testWorld;

import com.Jeka8833.GenomeTests.testWorld.objects.*;
import com.Jeka8833.GenomeTests.world.Cell;
import com.Jeka8833.GenomeTests.world.World;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.Range;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class TreeLive implements Serializable {
    // 0000 0000  0000 0000  0000 0000  0000 0000
    //
    // 00 - (00 - value != CellY; 01 - value < CellY; 10 - value > CellY; 11 - value == CellY) - condition1
    // 0 - If condition is true, then jump to gen
    // 0 - If condition is true, then create new Tree Blocks
    //
    // 00 - Block count
    // 0 - Jump to gen bit 1
    // 0 - Jump to gen bit 2
    //
    // 0 - Jump to gen bit 3
    // 0 - Jump to gen bit 4
    // 0 - Jump to gen bit 5
    // 0 - Left
    //
    // 0 - Top
    // 0 - Right
    // 0 - Bottom
    //
    // 0 - Priority Seed
    // 00 - Create Seed  (Sets only air in wood)
    //
    // 0 - Priority Sheet
    // 00 - Create Sheet (Sets only air in wood)
    //
    // 0 - Priority Wood
    // 00 - Create Wood  (Root - In ground)
    //
    // 0000 0000 - Value

    private static final int PARAM_CONDITION = 0b1100_0000__0000_0000__0000_0000__0000_0000;    // Mask
    private static final int PARAM_CONDITION_JUMP = 0b0010_0000__0000_0000__0000_0000__0000_0000;
    private static final int PARAM_CONDITION_CREATE = 0b0001_0000__0000_0000__0000_0000__0000_0000;

    private static final int PARAM_BLOCK_COUNT = 0b0000_1100__0000_0000__0000_0000__0000_0000;  // Mask
    private static final int PARAM_JUMP = 0b0000_0011__1110_0000__0000_0000__0000_0000;    // Mask
    private static final int PARAM_CREATE_RIGHT = 0b0000_0000__0001_0000__0000_0000__0000_0000;

    private static final int PARAM_CREATE_LEFT = 0b0000_0000__0000_1000__0000_0000__0000_0000;
    private static final int PARAM_CREATE_TOP = 0b0000_0000__0000_0100__0000_0000__0000_0000;
    private static final int PARAM_CREATE_BOTTOM = 0b0000_0000__0000_0010__0000_0000__0000_0000;
    private static final int PARAM_COUNT_SEED = 0b0000_0000__0000_0001__1100_0000__0000_0000;
    private static final int PARAM_COUNT_SHEET = 0b0000_0000__0000_0000__0011_1000__0000_0000;
    private static final int PARAM_COUNT_WOOD = 0b0000_0000__0000_0000__0000_0111__0000_0000;

    private static final int PARAM_VALUE = 0b0000_0000__0000_0000__0000_0000__1111_1111;    // Mask

    private final Genome genome;
    private volatile boolean isDead = false;
    private final AtomicInteger heath = new AtomicInteger();

    private final World world;
    private final int startTick;

    public TreeLive(final Genome genome, World world) {
        this.genome = genome;
        this.world = world;
        startTick = world.getTickCount();
    }

    public void addHeath(int heath) {
        this.heath.getAndAdd(heath);
    }

    public int getHeath() {
        return heath.get();
    }

    public boolean isDead() {
        if (isDead) return true;

        if (heath.get() <= 0 || world.getTickCount() - startTick > 2000) isDead = true;

        if (isDead && world != null && world.getGenerator() instanceof SimpleWorldGenerator generator) {
            generator.getGenomes().add(new SimpleWorldGenerator.GenomeOrder(world.getTickCount(), getGenome()));
        }
        return isDead;
    }

    public boolean addHeathAndCheckDead(int addValue) {
        if (isDead) return true;

        int heath = this.heath.addAndGet(addValue);
        if (heath <= 0 || world.getTickCount() - startTick > 2000) isDead = true;

        if (isDead && world != null && world.getGenerator() instanceof SimpleWorldGenerator generator) {
            generator.getGenomes().add(new SimpleWorldGenerator.GenomeOrder(world.getTickCount(), getGenome()));
        }
        return isDead;
    }

    public Genome getGenome() {
        return genome;
    }

    public void useGen(Cell cell, TreeBlock treeBlock) {
        useGen(cell, treeBlock.getStartGen());
    }

    public void useGen(Cell cell, int startGenome) {
        int chromosome = getGenome().chromosomes()[Math.abs(startGenome) % genome.chromosomes().length];

        boolean condition = checkCondition(chromosome, cell.y);

        boolean activateJump = (chromosome & PARAM_CONDITION_JUMP) != PARAM_CONDITION_JUMP;
        boolean activateCreate = (chromosome & PARAM_CONDITION_CREATE) != PARAM_CONDITION_CREATE;
        if (!activateJump) activateJump = condition;
        if (!activateCreate) activateCreate = condition;

        int nextGenIndex = activateJump ? (chromosome & PARAM_JUMP) >>> 21 : startGenome + 1;

        if (activateCreate) {
            new BlockCreator(cell, chromosome, world.getTickCount() - startTick > 10).placeBlock(this, nextGenIndex);
        }
    }

    public static TreeLive newTree(Genome parents, World world) {
        var newTree = new TreeLive(parents == null ? Genome.createGenome(16) : parents, world);

        newTree.addHeath(1000); // 1000 HP
        return newTree;
    }

    private boolean checkCondition(int chromosome, int value) {
        int condition = (chromosome & PARAM_CONDITION) >>> 30;
        int checkedValue = chromosome & PARAM_VALUE;

        return switch (condition) {
            case 0 -> value != checkedValue;
            case 1 -> value < checkedValue;
            case 2 -> value > checkedValue;
            case 3 -> value == checkedValue;
            default -> false;
        };
    }

    public World getWorld() {
        return world;
    }

    private static class BlockCreator {
        private final Cell cell;
        private final int chromosome;

        private @Range(from = 0, to = 0b111) int wood;
        private @Range(from = 0, to = 0b111) int sheet;
        private @Range(from = 0, to = 0b111) int seed;

        private BlockCreator(Cell cell, int chromosome, boolean allowSeeds) {
            this.cell = cell;
            this.chromosome = chromosome;
            wood = (chromosome & PARAM_COUNT_WOOD) >> 8;
            sheet = (chromosome & PARAM_COUNT_SHEET) >> 11;
            seed = allowSeeds ? (chromosome & PARAM_COUNT_SEED) >> 14 : 0;
        }

        public void placeBlock(TreeLive tree, int nextGenIndex) {
            int blockCount = (chromosome & PARAM_BLOCK_COUNT) >> 26;
            for (int i = 0; i < blockCount; i++) {
                int nextBlock = nextBlockType();
                if (nextBlock == 0) return;

                if ((chromosome & PARAM_CREATE_TOP) == PARAM_CREATE_TOP) {
                    Cell next = cell.world.getShiftedCell(cell.x, cell.y, 0, 1);
                    if (isAllowPlace(cell, next, nextBlock)) {
                        downPriorityAndPlace(tree, next, nextGenIndex, nextBlock);
                        continue;
                    }
                }
                if ((chromosome & PARAM_CREATE_RIGHT) == PARAM_CREATE_RIGHT) {
                    Cell next = cell.world.getShiftedCell(cell.x, cell.y, 1, 0);
                    if (isAllowPlace(cell, next, nextBlock)) {
                        downPriorityAndPlace(tree, next, nextGenIndex, nextBlock);
                        continue;
                    }
                }
                if ((chromosome & PARAM_CREATE_LEFT) == PARAM_CREATE_LEFT) {
                    Cell next = cell.world.getShiftedCell(cell.x, cell.y, -1, 0);
                    if (isAllowPlace(cell, next, nextBlock)) {
                        downPriorityAndPlace(tree, next, nextGenIndex, nextBlock);
                        continue;
                    }
                }
                if ((chromosome & PARAM_CREATE_BOTTOM) == PARAM_CREATE_BOTTOM) {
                    Cell next = cell.world.getShiftedCell(cell.x, cell.y, 0, -1);
                    if (isAllowPlace(cell, next, nextBlock)) {
                        downPriorityAndPlace(tree, next, nextGenIndex, nextBlock);
                        continue;
                    }
                }
                switch (nextBlock) {
                    case PARAM_COUNT_WOOD -> wood = 0;
                    case PARAM_COUNT_SHEET -> sheet = 0;
                    case PARAM_COUNT_SEED -> seed = 0;
                }
                i--;
            }
        }

        @MagicConstant(flags = {0, PARAM_COUNT_WOOD, PARAM_COUNT_SHEET, PARAM_COUNT_SEED})
        private int nextBlockType() {
            int max = Math.max(Math.max(calcPriority(wood), calcPriority(sheet)), calcPriority(seed));
            if (max == 0) return 0;

            if (max == calcPriority(wood)) return PARAM_COUNT_WOOD;
            if (max == calcPriority(sheet)) return PARAM_COUNT_SHEET;
            return PARAM_COUNT_SEED;
        }

        private void downPriorityAndPlace(TreeLive tree, Cell cell, int nextGenIndex, int blockType) {
            switch (blockType) {
                case PARAM_COUNT_WOOD -> {
                    cell.layers.add(new Wood(tree, nextGenIndex));
                    wood = downPriority(wood);
                }
                case PARAM_COUNT_SHEET -> {
                    cell.layers.add(new Sheet(tree, nextGenIndex));
                    sheet = downPriority(sheet);
                }
                case PARAM_COUNT_SEED -> {
                    cell.layers.add(new Seed(tree, nextGenIndex));
                    seed = downPriority(seed);
                }
            }
        }

        @Range(from = 0, to = 0b111)
        private static int calcPriority(@Range(from = 0, to = 0b111) int value) {
            return (value & 0b11) > 0 ? value : 0;
        }

        @Range(from = 0, to = 0b110)
        private static int downPriority(@Range(from = 0, to = 0b111) int value) {
            return (value & 0b11) > 0 ? value - 1 : 0;
        }

        private static boolean isAllowPlace(Cell current, Cell next, int blockType) {
            // Only air or ground
            if (next == null || !(next.layers.isEmpty() ||
                    (next.layers.size() == 1 && next.containsLayer(Grass.class)))) return false;

            return switch (blockType) {
                case PARAM_COUNT_WOOD -> true;
                case PARAM_COUNT_SHEET -> current.containsLayer(Wood.class) && next.layers.isEmpty();
                case PARAM_COUNT_SEED -> current.containsLayer(Wood.class) && next.layers.isEmpty();
                default -> false;
            };
        }
    }
}
