package com.Jeka8833.GenomeTests.testWorld.objects;

import com.Jeka8833.GenomeTests.testWorld.TreeLive;
import com.Jeka8833.GenomeTests.world.Cell;

public class Wood extends TreeBlock {

    public Wood(TreeLive treeLive, int startGen) {
        super(treeLive, startGen);
    }

    @Override
    public void tick(Cell cell) {
        if (!isUseGen()) {
            getTreeLive().useGen(cell, this);
            setUseGen(true);
        }
        getTreeLive().addHeath(-5);
        if (getTreeLive().isDead()) cell.layers.remove(this);
    }
}
