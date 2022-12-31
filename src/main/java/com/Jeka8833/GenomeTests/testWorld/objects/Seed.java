package com.Jeka8833.GenomeTests.testWorld.objects;

import com.Jeka8833.GenomeTests.testWorld.Genome;
import com.Jeka8833.GenomeTests.testWorld.SimpleWorldGenerator;
import com.Jeka8833.GenomeTests.testWorld.TreeLive;
import com.Jeka8833.GenomeTests.world.Cell;
import com.Jeka8833.GenomeTests.world.World;

import java.util.Random;

public class Seed extends TreeBlock {

    private static final Random RANDOM = new Random();

    public Seed(TreeLive treeLive, int startGen) {
        super(treeLive, startGen);
    }

    public Seed(Genome genome, World world) {
        super(new TreeLive(genome, world), genome.startIndex());
    }

    @Override
    public void tick(Cell cell) {
        if (getTreeLive().isDead()) {
            cell.layers.remove(this);

            int move = Integer.signum(SimpleWorldGenerator.GROUND_LEVEL - 1 - cell.y);
            if (move == 0) {
                Genome newGenome = RANDOM.nextBoolean() ?
                        getTreeLive().getGenome().mutation(4) : getTreeLive().getGenome();
                TreeLive.newTree(newGenome, getTreeLive().getWorld()).useGen(cell, newGenome.startIndex());
            } else {
                if(move > 0) return;

                Cell bottomCell = cell.world.getShiftedCell(cell.x, cell.y, 0, move);
                if (bottomCell != null) {
                    if (bottomCell.layers.isEmpty() ||
                            (bottomCell.layers.size() == 1 && bottomCell.containsLayer(Grass.class))) {
                        bottomCell.layers.add(this);
                    }
                }
            }
        } else {
            getTreeLive().addHeath(-1);
        }
    }
}
