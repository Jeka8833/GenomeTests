package com.Jeka8833.GenomeTests.console.commands;

import com.Jeka8833.GenomeTests.util.WorldManager;
import com.Jeka8833.GenomeTests.world.World;
import com.Jeka8833.GenomeTests.world.WorldSimulation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

@CommandLine.Command(name = "remove", mixinStandardHelpOptions = true)
public class RemoveCommand implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(ViewCommand.class);

    private final WorldManager worldManager;

    @CommandLine.Option(names = "-w", defaultValue = "all")
    public String[] worlds;

    public RemoveCommand(WorldManager worldManager) {
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
                simulation.stop();
                worldManager.remove(simulation);
            }
        }
    }
}
