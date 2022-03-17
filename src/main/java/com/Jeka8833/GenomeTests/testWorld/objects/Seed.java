package com.Jeka8833.GenomeTests.testWorld.objects;

import com.Jeka8833.GenomeTests.testWorld.SimpleWorldGenerator;
import com.Jeka8833.GenomeTests.testWorld.TreeLive;
import com.Jeka8833.GenomeTests.world.Cell;

public class Seed extends TreeBlock {
    public Seed(TreeLive treeLive, int startGen) {
        super(treeLive, startGen);
    }

    @Override
    public void tick(Cell cell) {
        if (!isUseGen()) {
            getTreeLive().useGen(cell, this);
            setUseGen(true);
        }

        if (getTreeLive().isDead()) {
            cell.layers.remove(this);
            Cell nextCell = cell.world.getShiftedCell(cell.x, cell.y,
                    0, (int) Math.signum((SimpleWorldGenerator.GROUND_LEVEL - 1) - cell.y));
            if (nextCell != null) {
                if (nextCell.layers.isEmpty()) {
                    nextCell.layers.add(this);
                } else if (nextCell.getLayer(Grass.class) != null) {
                    nextCell.layers.add(new Seed(TreeLive.newTree(getTreeLive()), 0));
                }
            }
        }
        getTreeLive().addHeath(-10);
    }
}
