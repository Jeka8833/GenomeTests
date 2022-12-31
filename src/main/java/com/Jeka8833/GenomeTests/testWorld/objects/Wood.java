package com.Jeka8833.GenomeTests.testWorld.objects;

import com.Jeka8833.GenomeTests.testWorld.TreeLive;
import com.Jeka8833.GenomeTests.world.Cell;

public class Wood extends TreeBlock {

    private int createTick = Integer.MIN_VALUE;

    public Wood(TreeLive treeLive, int startGen) {
        super(treeLive, startGen);
    }

    @Override
    public void tick(Cell cell) {
        if (getTreeLive().addHeathAndCheckDead(-10)) {
            cell.layers.remove(this);
        } else {
            Grass grass = cell.getLayer(Grass.class);
            if (grass != null) {
                getTreeLive().addHeath(Math.min((int) grass.takeEnergy(), 7));
            }
            if (createTick == Integer.MIN_VALUE) createTick = cell.world.getTickCount();

            if (createTick + 1 == cell.world.getTickCount()) {
                getTreeLive().useGen(cell, this);
            }
        }
    }
}
