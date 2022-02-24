package com.Jeka8833.GenomeTests.testWorld.objects;

import com.Jeka8833.GenomeTests.testWorld.TreeLive;
import com.Jeka8833.GenomeTests.world.CellLayers;

public abstract class TreeBlock implements CellLayers {

    public final TreeLive treeLive;
    private boolean isGrow = true;
    private int startGen = 0;

    public TreeBlock(TreeLive treeLive, int startGen) {
        this.startGen = startGen;
        this.treeLive = treeLive;
        treeLive.registerBlock(this);
    }

    public TreeLive getTreeLive() {
        return treeLive;
    }

    public boolean isGrow() {
        return isGrow;
    }

    public void setGrow(boolean grow) {
        isGrow = grow;
    }

    public int getStartGen() {
        return startGen;
    }

    public void setStartGen(int startGen) {
        this.startGen = startGen;
    }
}
