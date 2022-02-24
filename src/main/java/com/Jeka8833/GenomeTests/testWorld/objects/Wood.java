package com.Jeka8833.GenomeTests.testWorld.objects;

import com.Jeka8833.GenomeTests.testWorld.TreeLive;
import com.Jeka8833.GenomeTests.world.Cell;

public class Wood extends TreeBlock {

    public Wood(TreeLive treeLive, int startGen) {
        super(treeLive, startGen);
    }

    @Override
    public void tick(Cell cell) {
        if (isGrow()){
            treeLive.useGen(cell, this);
        }
        treeLive.addHeath(-1);
    }
}
