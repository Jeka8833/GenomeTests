package com.Jeka8833.GenomeTests;

import com.Jeka8833.GenomeTests.console.ConsoleGUI;
import com.Jeka8833.GenomeTests.console.ConsoleHook;
import com.Jeka8833.GenomeTests.testWorld.FrameManager;
import com.Jeka8833.GenomeTests.testWorld.SimpleWorldGenerator;
import com.Jeka8833.GenomeTests.world.World;
import com.Jeka8833.GenomeTests.world.WorldTimeManager;
import com.Jeka8833.GenomeTests.world.visualize.WorldFrame;

public class Main {

    public static void main(String[] args) {
        ConsoleHook.initHook();

        WorldFrame.init();

        var worldTimeManager = new WorldTimeManager();
        World world = SimpleWorldGenerator.createWorld();
        world.setLimitTickPerMinute(0);
        worldTimeManager.addWorld(world);
        //worldTimeManager.start();

        ConsoleGUI consoleGUI = ConsoleGUI.create();
        consoleGUI.setTimeManager(worldTimeManager);

        WorldFrame.createWindow(world, new FrameManager());
        Runtime.getRuntime().addShutdownHook(new Thread(WorldFrame::close));
    }
}
