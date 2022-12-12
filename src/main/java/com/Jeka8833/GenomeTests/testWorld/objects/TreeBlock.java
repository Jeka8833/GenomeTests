package com.Jeka8833.GenomeTests.testWorld.objects;

import com.Jeka8833.GenomeTests.testWorld.TreeLive;
import com.Jeka8833.GenomeTests.world.CellLayers;

public abstract class TreeBlock implements CellLayers {

    private final TreeLive treeLive;
    private int startGen;

    public TreeBlock(TreeLive treeLive, int startGen) {
        this.startGen = startGen;
        this.treeLive = treeLive;
    }

    public TreeLive getTreeLive() {
        return treeLive;
    }

    public int getStartGen() {
        return startGen;
    }

    public void setStartGen(int startGen) {
        this.startGen = startGen;
    }
}
