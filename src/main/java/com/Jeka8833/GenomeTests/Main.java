package com.Jeka8833.GenomeTests;

import com.Jeka8833.GenomeTests.console.ConsoleGUI;
import com.Jeka8833.GenomeTests.console.ConsoleHook;
import com.Jeka8833.GenomeTests.testWorld.FrameManager;
import com.Jeka8833.GenomeTests.testWorld.SimpleWorldGenerator;
import com.Jeka8833.GenomeTests.util.WorldManager;
import com.Jeka8833.GenomeTests.world.World;
import com.Jeka8833.GenomeTests.world.WorldReplay;
import com.Jeka8833.GenomeTests.world.WorldSimulation;
import com.Jeka8833.GenomeTests.world.visualize.WindowManager;

import java.io.IOException;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        ConsoleHook.initHook();

        WorldManager worldManager = new WorldManager();
        World world = SimpleWorldGenerator.createWorld(worldManager, 0, null);
        WorldSimulation simulation = worldManager.add(world);
        /*try {
            simulation.setCreateReplay(new WorldReplay(Path.of("D:\\User\\Download\\World\\"), 3, 16));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/

        WindowManager.createWindow(world, new FrameManager());

        ConsoleGUI consoleGUI = ConsoleGUI.create();
        consoleGUI.setTimeManager(worldManager);

        Runtime.getRuntime().addShutdownHook(new Thread(WindowManager::clearAll));
    }
}
