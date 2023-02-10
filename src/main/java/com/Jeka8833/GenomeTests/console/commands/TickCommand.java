package com.Jeka8833.GenomeTests.console.commands;

import com.Jeka8833.GenomeTests.util.WorldManager;
import com.Jeka8833.GenomeTests.world.World;
import com.Jeka8833.GenomeTests.world.WorldSimulation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

@CommandLine.Command(name = "tick", mixinStandardHelpOptions = true)
public class TickCommand implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(ViewCommand.class);

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;
    private final WorldManager worldManager;

    @CommandLine.Option(names = "-w", defaultValue = "all")
    public String[] worlds;

    @CommandLine.Option(names = "-c", defaultValue = "1")
    public int count = 1;

    public TickCommand(WorldManager worldManager) {
        this.worldManager = worldManager;
    }

    @Override
    public void run() {
        if (count < 1) throw new CommandLine.ParameterException(spec.commandLine(),
                String.format("Invalid value '%s' for option '-c'. Range: [1-" + Integer.MAX_VALUE + "]", count));

        if (worlds.length > 0 && worlds[0].equalsIgnoreCase("all"))
            worlds = worldManager.getWorlds().stream()
                    .map(World::getName)
                    .toArray(String[]::new);

        for (String worldNames : worlds) {
            WorldSimulation simulation = worldManager.getSimulation(worldNames);
            if (simulation == null) {
                LOGGER.warn("World not found '" + worldNames + "'");
            } else {
                for (int i = 0; i < count; i++) {
                    try {
                        simulation.getWorld().tick();
                    } catch (Exception e) {
                        LOGGER.warn("World tick error '" + worldNames + "'", e);
                    }
                }
            }
        }
        LOGGER.info("The tick command has been completed");
    }
}
