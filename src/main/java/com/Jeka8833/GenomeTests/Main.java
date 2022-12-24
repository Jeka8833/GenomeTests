package com.Jeka8833.GenomeTests;

import com.Jeka8833.GenomeTests.console.ConsoleGUI;
import com.Jeka8833.GenomeTests.console.ConsoleHook;
import com.Jeka8833.GenomeTests.testWorld.FrameManager;
import com.Jeka8833.GenomeTests.testWorld.SimpleWorldGenerator;
import com.Jeka8833.GenomeTests.util.WorldManager;
import com.Jeka8833.GenomeTests.world.World;
import com.Jeka8833.GenomeTests.world.visualize.WindowManager;

public class Main {

    public static void main(String[] args) {
        ConsoleHook.initHook();

        WorldManager worldManager = new WorldManager();
        World world = SimpleWorldGenerator.createWorld(worldManager, 0, null);
        worldManager.add(world);

        WindowManager.createWindow(world, new FrameManager());

        ConsoleGUI consoleGUI = ConsoleGUI.create();
        consoleGUI.setTimeManager(worldManager);

        Runtime.getRuntime().addShutdownHook(new Thread(WindowManager::clearAll));
    }
}
