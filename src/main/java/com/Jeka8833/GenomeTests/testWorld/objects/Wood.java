package com.Jeka8833.GenomeTests.testWorld.objects;

import com.Jeka8833.GenomeTests.testWorld.TreeLive;
import com.Jeka8833.GenomeTests.world.Cell;

public class Wood extends TreeBlock {

    private boolean isGrew = false;

    public Wood(TreeLive treeLive, int startGen) {
        super(treeLive, startGen);
    }

    @Override
    public void tick(Cell cell) {
        getTreeLive().addHeath(-3);

        if (getTreeLive().isDead()) {
            cell.layers.remove(this);
        } else {
            if (!isGrew) {
                getTreeLive().useGen(cell, this);
                isGrew = true;
            }
        }
    }
}
