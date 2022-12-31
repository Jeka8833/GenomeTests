package com.Jeka8833.GenomeTests;

import com.Jeka8833.GenomeTests.console.ConsoleGUI;
import com.Jeka8833.GenomeTests.console.ConsoleHook;
import com.Jeka8833.GenomeTests.testWorld.FrameManager;
import com.Jeka8833.GenomeTests.testWorld.SimpleWorldGenerator;
import com.Jeka8833.GenomeTests.util.WorldManager;
import com.Jeka8833.GenomeTests.world.SimulationSynchronizer;
import com.Jeka8833.GenomeTests.world.World;
import com.Jeka8833.GenomeTests.world.WorldSimulation;
import com.Jeka8833.GenomeTests.world.visualize.WindowManager;

public class Main {

    public static void main(String[] args) {
        ConsoleHook.initHook();

        SimulationSynchronizer synchronize = new SimulationSynchronizer();
        WorldManager worldManager = new WorldManager();
        World world1 = SimpleWorldGenerator.createWorld(worldManager, 0, null);
        World world2 = SimpleWorldGenerator.createWorld(worldManager, 0, null);
        world2.setName("World1-0");
        world2.setName("World2-0");
        WorldSimulation simulation1 = worldManager.add(world1);
        WorldSimulation simulation2 = worldManager.add(world2);
        simulation1.setSynchronizer(synchronize);
        simulation2.setSynchronizer(synchronize);

        /*try {
            simulation.setCreateReplay(new WorldReplay(Path.of("D:\\User\\Download\\World\\"), 3, 16));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/

        WindowManager.createWindow(world1, new FrameManager());
        WindowManager.createWindow(world2, new FrameManager());

        ConsoleGUI consoleGUI = ConsoleGUI.create();
        consoleGUI.setTimeManager(worldManager);

        Runtime.getRuntime().addShutdownHook(new Thread(WindowManager::clearAll));
    }
}
