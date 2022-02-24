package com.Jeka8833.GenomeTests.testWorld.objects;

import com.Jeka8833.GenomeTests.testWorld.TreeLive;
import com.Jeka8833.GenomeTests.world.Cell;

public class Seed extends TreeBlock {
    public Seed(TreeLive treeLive, int startGen) {
        super(treeLive, startGen);
    }

    @Override
    public void tick(Cell cell) {
        if (treeLive.isDead()) {
            cell.layers.remove(this);
            Cell downCell = cell.world.getCell(cell.x, cell.y + 1);
            if (downCell != null) {
                if (downCell.layers.isEmpty()) {
                    downCell.layers.add(this);
                } else if (downCell.getLayer(Grass.class) != null) {
                    downCell.layers.add(new Seed(TreeLive.newTree(treeLive), 0));
                }
            }
        } else {
            if (treeLive.getTreeBlocks().size() == 1) {
                if (isGrow()) {
                    treeLive.useGen(cell, this);
                    cell.layers.remove(this);
                }
            }
        }
        treeLive.addHeath(-1);
    }
}
