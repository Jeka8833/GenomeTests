package com.Jeka8833.GenomeTests.console.console.commands;

import com.Jeka8833.GenomeTests.util.WorldManager;
import com.Jeka8833.GenomeTests.world.World;
import com.Jeka8833.GenomeTests.world.WorldSimulation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

@CommandLine.Command(name = "restart", mixinStandardHelpOptions = true)
public class RestartCommand implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(ViewCommand.class);

    private final WorldManager worldManager;

    @CommandLine.Option(names = "-w", defaultValue = "all")
    public String[] worlds;

    public RestartCommand(WorldManager worldManager) {
        this.worldManager = worldManager;
    }

    @Override
    public void run() {
        if (worlds.length > 0 && worlds[0].equalsIgnoreCase("all"))
            worlds = worldManager.getWorlds().stream()
                    .map(World::getName)
                    .toArray(String[]::new);

        for (String worldNames : worlds) {
            WorldSimulation simulation = worldManager.getSimulation(worldNames);
            if (simulation == null) {
                LOGGER.warn("World not found '" + worldNames + "'");
            } else {
                try {
                    simulation.stopAndWait();
                } catch (InterruptedException e) {
                    LOGGER.error("waiting for full stop the world '" + worldNames + "'", e);
                }
                worldManager.remove(simulation);

                World world = simulation.getWorld();
                World clone = new World(world.getWidth(), world.getHeight(), world.getGenerator());
                clone.setName(worldNames);
                clone.setThreadCount(world.getThreadCount());

                try {
                    worldManager.add(clone).start();
                } catch (InterruptedException e) {
                    LOGGER.warn("Fail start world '" + worldNames + "'");
                }
            }
        }
    }
}
