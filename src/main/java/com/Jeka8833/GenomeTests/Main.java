package com.Jeka8833.GenomeTests;

import com.Jeka8833.GenomeTests.console.ConsoleGUI;
import com.Jeka8833.GenomeTests.console.ConsoleHook;
import com.Jeka8833.GenomeTests.testWorld.SimpleWorldGenerator;
import com.Jeka8833.GenomeTests.world.WorldTimeManager;
import com.Jeka8833.GenomeTests.world.visualize.WorldFrame;

public class Main {

    public static void main(String[] args) {
        ConsoleHook.initHook();

        WorldFrame.init();

        var worldTimeManager = new WorldTimeManager();
        SimpleWorldGenerator.createWorld(worldTimeManager, 0);
        //worldTimeManager.start();

        ConsoleGUI consoleGUI = ConsoleGUI.create();
        consoleGUI.setTimeManager(worldTimeManager);

        Runtime.getRuntime().addShutdownHook(new Thread(WorldFrame::close));
    }
}
