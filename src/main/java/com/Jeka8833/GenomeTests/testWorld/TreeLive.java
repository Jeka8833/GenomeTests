package com.Jeka8833.GenomeTests.testWorld;

import com.Jeka8833.GenomeTests.testWorld.objects.*;
import com.Jeka8833.GenomeTests.world.Cell;
import org.intellij.lang.annotations.MagicConstant;

import java.io.Serializable;
import java.util.*;

public class TreeLive implements Serializable {
    // 0000 0000  0000 0000  0000 0000  0000 0000
    //
    // 00 - (00 - value != CellY; 01 - value < CellY; 10 - value > CellY; 11 - value == CellY) - condition1
    // 00 - (00 - value != Health; 01 - value < Health; 10 - value > Health; 11 - value == Health) - condition2
    //
    // 0 - Invert condition1
    // 0 - Invert condition2
    // 00 - (00 - condition1 != condition2; 01 - condition1 || condition2;
    //       10 - condition1 && condition2; 11 - condition1 == condition2)
    //
    //
    // 0 - If condition is true, then jump to gen
    // 0 - If condition is true, then create new Tree Blocks
    // 0 - Always jump to gen
    // 0 - Always create new Tree Blocks
    //
    // 0 - Jump to gen bit 1
    // 0 - Jump to gen bit 2
    // 0 - Jump to gen bit 3
    // 0 - Jump to gen bit 4
    //
    //
    // 0 - Jump to gen bit 5
    // 0 - Left
    // 0 - Top
    // 0 - Right
    //
    // 0 - Bottom
    // 0 - Create Seed  (Sets only air in wood)
    // 0 - Create Sheet (Sets only air in wood)
    // 0 - Create Wood  (Root - In ground)
    //
    //
    // 0000 0000 - Value

    // format:off
    private static final int PARAM_CONDITION1    = 0b1100_0000__0000_0000__0000_0000__0000_0000;    // Mask
    private static final int PARAM_CONDITION2    = 0b0011_0000__0000_0000__0000_0000__0000_0000;    // Mask

    private static final int PARAM_INVERT_C1     = 0b0000_1000__0000_0000__0000_0000__0000_0000;
    private static final int PARAM_INVERT_C2     = 0b0000_0100__0000_0000__0000_0000__0000_0000;
    private static final int PARAM_OPERATOR      = 0b0000_0011__0000_0000__0000_0000__0000_0000;    // Mask

    private static final int PARAM_C_JUMP        = 0b0000_0000__1000_0000__0000_0000__0000_0000;
    private static final int PARAM_C_CREATE      = 0b0000_0000__0100_0000__0000_0000__0000_0000;
    private static final int PARAM_A_JUMP        = 0b0000_0000__0010_0000__0000_0000__0000_0000;
    private static final int PARAM_A_CREATE      = 0b0000_0000__0001_0000__0000_0000__0000_0000;

    private static final int PARAM_JUMP          = 0b0000_0000__0000_1111__1000_0000__0000_0000;    // Mask

    private static final int PARAM_CREATE_SIDES  = 0b0000_0000__0000_0000__0111_1000__0000_0000;    // Mask
    private static final int PARAM_CREATE_RIGHT  = 0b0000_0000__0000_0000__0100_0000__0000_0000;
    private static final int PARAM_CREATE_LEFT   = 0b0000_0000__0000_0000__0010_0000__0000_0000;
    private static final int PARAM_CREATE_TOP    = 0b0000_0000__0000_0000__0001_0000__0000_0000;

    private static final int PARAM_CREATE_BOTTOM = 0b0000_0000__0000_0000__0000_1000__0000_0000;
    private static final int PARAM_CREATE_TYPE   = 0b0000_0000__0000_0000__0000_0111__0000_0000;    // Mask
    private static final int PARAM_CREATE_SEED   = 0b0000_0000__0000_0000__0000_0100__0000_0000;
    private static final int PARAM_CREATE_SHEET  = 0b0000_0000__0000_0000__0000_0010__0000_0000;
    private static final int PARAM_CREATE_WOOD   = 0b0000_0000__0000_0000__0000_0001__0000_0000;

    private static final int PARAM_VALUE         = 0b0000_0000__0000_0000__0000_0000__1111_1111;    // Mask
    // format:on

    private static final Comparator<TreeLive.Block> comparator = Comparator.nullsLast(Comparator.comparing(Block::priority));
    public static final List<TreeLive> treeList = new ArrayList<>();
    private static final Random RANDOM = new Random();

    private final Genome genome;
    private final int id;
    private int heath = 0;

    protected TreeLive(int id, final Genome genome) {
        this.id = id;
        this.genome = genome;
    }

    public int getId() {
        return id;
    }

    public void addHeath(int heath) {
        this.heath += heath;
    }

    public boolean isDead() {
        return heath <= 0;
    }

    public Genome getGenome() {
        return genome;
    }

    public void useGen(Cell cell, TreeBlock treeBlock) {
        int gen = getGenome().getChromosomes()[treeBlock.getStartGen()];

        boolean condition1 = checkCondition(gen, cell.y, PARAM_CONDITION1, PARAM_INVERT_C1);
        boolean condition2 = checkCondition(
                gen, Math.min(PARAM_VALUE, treeBlock.getTreeLive().heath), PARAM_CONDITION2, PARAM_INVERT_C2);

        int operator = (gen & PARAM_OPERATOR) >>> 24;
        boolean conditionState = switch (operator) {
            case 0:
                yield condition1 != condition2;
            case 1:
                yield condition1 || condition2;
            case 2:
                yield condition1 && condition2;
            case 3:
                yield condition1 == condition2;
            default:
                throw new IllegalStateException("Unexpected value: " + operator);
        };

        boolean activateJump = (gen & PARAM_A_JUMP) == PARAM_A_JUMP;
        boolean activateCreate = (gen & PARAM_A_CREATE) == PARAM_A_CREATE;
        if (conditionState && (gen & PARAM_C_JUMP) == PARAM_C_JUMP) activateJump = true;
        if (conditionState && (gen & PARAM_C_CREATE) == PARAM_C_CREATE) activateCreate = true;

        if (activateJump) {
            int jumpIndex = (gen & PARAM_JUMP) >>> 15;
            if (jumpIndex < treeBlock.getTreeLive().getGenome().getChromosomes().length)
                treeBlock.setStartGen(jumpIndex);
        }
        if (activateCreate) setBlocks(gen, cell, this, treeBlock.getStartGen(), 4);
    }

    public static TreeLive newTree(TreeLive parents) {
        if (parents == null) {
            Genome genome = Genome.createGenome(16);
            var treeLive = new TreeLive(genome.getId(), genome);
            treeLive.addHeath(RANDOM.nextInt(50)); // 0-50  HP
            treeList.add(treeLive);
            return treeLive;
        } else {
            if (RANDOM.nextInt(101) <= 20) { // Random 20%
                Genome genome = parents.getGenome().mutation(0.1f); // 10% Mutation
                var treeLive = new TreeLive(genome.getId(), genome);
                treeLive.addHeath(5 + RANDOM.nextInt(15)); // 5-20 HP
                treeList.add(treeLive);
                return treeLive;
            }
        }
        var treeLive = new TreeLive(parents.getId(), parents.getGenome()); // Clone genome
        treeLive.addHeath(RANDOM.nextInt(50)); // 0-50 HP
        treeList.add(treeLive);
        return treeLive;
    }

    private static void setBlocks(int gen, Cell cell, TreeLive live,
                                  int startGen,
                                  int maxCount) {
        if ((gen & PARAM_CREATE_SIDES) == 0 || (gen & PARAM_CREATE_TYPE) == 0) return;
        var blocks = new Block[]{
                Block.createBlock(cell, gen, PARAM_CREATE_LEFT),
                Block.createBlock(cell, gen, PARAM_CREATE_RIGHT),
                Block.createBlock(cell, gen, PARAM_CREATE_TOP),
                Block.createBlock(cell, gen, PARAM_CREATE_BOTTOM)
        };
        for (int i = 0; i < maxCount; i++) {
            Arrays.sort(blocks, comparator);
            if (blocks[0] == null || blocks[0].isUsed()) return;
            int used = blocks[0].useBlock(live, startGen);
            for (int j = 1; j < 4; j++) {
                if (blocks[j] == null || blocks[j].isUsed()) break;
                switch (used) {
                    case PARAM_CREATE_WOOD -> blocks[j].downPriorityWood();
                    case PARAM_CREATE_SHEET -> blocks[j].downPrioritySheet();
                    case PARAM_CREATE_SEED -> blocks[j].downPrioritySeed();
                }
            }
        }
    }

    // TODO: Bugged external annotations, can be removed if needed
    private static boolean checkCondition(int gen, int checkedValue,
                                          @MagicConstant(flags = {PARAM_CONDITION1, PARAM_CONDITION2}) int paramCondition,
                                          @MagicConstant(flags = {PARAM_INVERT_C1, PARAM_INVERT_C2}) int paramInvertC) {
        int value = gen & PARAM_VALUE;
        checkedValue &= PARAM_VALUE;

        int condition = switch (paramCondition) {
            case PARAM_CONDITION1:
                yield (gen & PARAM_CONDITION1) >>> 30;
            case PARAM_CONDITION2:
                yield (gen & PARAM_CONDITION2) >>> 28;
            default:
                throw new IllegalStateException("Unexpected value: " + paramCondition);
        };

        boolean state = switch (condition) {
            case 0:
                yield value != checkedValue;
            case 1:
                yield value < checkedValue;
            case 2:
                yield value > checkedValue;
            case 3:
                yield value == checkedValue;
            default:
                throw new IllegalStateException("Unexpected value: " + condition);
        };
        return ((gen & paramInvertC) == paramInvertC) != state;
    }

    private static class Block {
        private static final int USED_VALUE = 100;

        private final Cell cell;
        private int wood;
        private int sheet;
        private int seed;

        private Block(Cell cell, int wood, int sheet, int seed) {
            this.cell = cell;
            this.wood = wood;
            this.sheet = sheet;
            this.seed = seed;
        }

        private int priority() {
            return wood + sheet + seed;
        }

        public void downPriorityWood() {
            wood++;
        }

        public void downPrioritySheet() {
            sheet++;
        }

        public void downPrioritySeed() {
            seed++;
        }

        private int useBlock(TreeLive tree, int startGen) {
            int minValue = Math.min(Math.min(wood, sheet), seed);
            if (minValue == wood) {
                cell.layers.add(new Wood(tree, startGen));
                wood = sheet = seed = USED_VALUE;
                return PARAM_CREATE_WOOD;
            } else if (minValue == sheet) {
                cell.layers.add(new Sheet(tree, startGen));
                wood = sheet = seed = USED_VALUE;
                return PARAM_CREATE_SHEET;
            } else {
                cell.layers.add(new Seed(tree, startGen));
                wood = sheet = seed = USED_VALUE;
                return PARAM_CREATE_SEED;
            }
        }

        public boolean isUsed() {
            return wood >= USED_VALUE && sheet >= USED_VALUE && seed >= USED_VALUE;
        }

        public Cell getCell() {
            return cell;
        }

        // TODO: Bugged external annotations, can be removed if needed
        private static Block createBlock(Cell cell, int gen,
                                         @MagicConstant(flags = {PARAM_CREATE_LEFT, PARAM_CREATE_RIGHT, PARAM_CREATE_TOP, PARAM_CREATE_BOTTOM})
                                                 int side) {
            if ((gen & side) != side) return null;
            Cell temp = switch (side) {
                case PARAM_CREATE_LEFT:
                    yield cell.world.getShiftedCell(cell.x, cell.y, -1, 0);
                case PARAM_CREATE_RIGHT:
                    yield cell.world.getShiftedCell(cell.x, cell.y, 1, 0);
                case PARAM_CREATE_TOP:
                    yield cell.world.getShiftedCell(cell.x, cell.y, 0, 1);
                case PARAM_CREATE_BOTTOM:
                    yield cell.world.getShiftedCell(cell.x, cell.y, 0, -1);
                default:
                    throw new IllegalStateException("Unexpected value: " + side);
            };
            if (temp == null || (!(temp.layers.isEmpty() || (temp.layers.size() == 1 && temp.getLayer(Grass.class) != null))))
                return null;

            boolean airWoodParents = cell.getLayer(Wood.class) != null && cell.getLayer(Grass.class) == null;

            var block = new Block(temp, (gen & PARAM_CREATE_WOOD) == PARAM_CREATE_WOOD ? 0 : USED_VALUE,
                    (gen & PARAM_CREATE_SHEET) == PARAM_CREATE_SHEET && airWoodParents ? 0 : USED_VALUE,
                    (gen & PARAM_CREATE_SEED) == PARAM_CREATE_SEED && airWoodParents ? 0 : USED_VALUE);
            if (block.priority() <= 0) return null;

            return block;
        }
    }
}
