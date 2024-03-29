package com.Jeka8833.GenomeTests.testWorld.objects;

import com.Jeka8833.GenomeTests.testWorld.TreeLive;
import com.Jeka8833.GenomeTests.world.Cell;

public class Sheet extends TreeBlock {

    public Sheet(TreeLive treeLive, int startGen) {
        super(treeLive, startGen);
    }

    @Override
    public void tick(Cell cell) {
        if (getTreeLive().addHeathAndCheckDead(-1)) cell.layers.remove(this);
    }
}
