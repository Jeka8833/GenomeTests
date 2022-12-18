package com.Jeka8833.GenomeTests.console.console.commands;

import com.Jeka8833.GenomeTests.util.FileSaver;
import com.Jeka8833.GenomeTests.util.WorldManager;
import com.Jeka8833.GenomeTests.world.World;
import com.Jeka8833.GenomeTests.world.WorldSimulation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@CommandLine.Command(name = "save", mixinStandardHelpOptions = true)
public class SaveCommand implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger(ViewCommand.class);

    private final WorldManager worldManager;

    @CommandLine.Option(names = "-w", defaultValue = "all")
    public String[] worlds;
    @CommandLine.Option(names = "-p", defaultValue = "save/")
    public Path path;

    public SaveCommand(WorldManager worldManager) {
        this.worldManager = worldManager;
    }

    @Override
    public void run() {
        if (worlds.length > 0 && worlds[0].equalsIgnoreCase("all"))
            worlds = worldManager.getWorlds().stream()
                    .map(World::getName)
                    .toArray(String[]::new);

        Path snapshot = path.resolve(System.currentTimeMillis() + "");
        try {
            Files.createDirectories(snapshot);
        } catch (IOException e) {
            LOGGER.error("Fail create snapshot folder", e);
        }

        for (String worldNames : worlds) {
            WorldSimulation simulation = worldManager.getSimulation(worldNames);
            try {
                FileSaver.saveToFile(snapshot.resolve(worldNames + ".dataworld"), simulation);
            } catch (IOException e) {
                LOGGER.error("Fail save world: " + worldNames, e);
            }
        }
    }
}
