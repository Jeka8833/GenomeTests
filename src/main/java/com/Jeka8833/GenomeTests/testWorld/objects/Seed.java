package com.Jeka8833.GenomeTests.testWorld.objects;

import com.Jeka8833.GenomeTests.testWorld.TreeLive;
import com.Jeka8833.GenomeTests.world.Cell;

public class Seed extends TreeBlock {

    public Seed(TreeLive treeLive, int startGen) {
        super(treeLive, startGen);
    }

    @Override
    public void tick(Cell cell) {
        if (cell.getLayer(Grass.class) != null) {
            // The seed in grass

            getTreeLive().addHeath(-1);

            cell.layers.remove(this);
            if (!getTreeLive().isDead()) getTreeLive().useGen(cell, this, getStartGen());
        } else if (cell.layers.size() == 1) {
            // The seed is falls or hangs from a tree.

            if (getTreeLive().isDead()) {
                cell.layers.remove(this);

                Cell bottomCell = cell.world.getShiftedCell(cell.x, cell.y, 0, -1);
                bottomCell.layers.add(this);
            } else {
                getTreeLive().addHeath(-1);
            }
        } else {
            // The seed are blocked by other things, it is dead.

            cell.layers.remove(this);
        }
    }
}
