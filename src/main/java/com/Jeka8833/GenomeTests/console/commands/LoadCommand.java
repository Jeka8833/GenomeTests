package com.Jeka8833.GenomeTests.console.commands;

import com.Jeka8833.GenomeTests.util.FileSaver;
import com.Jeka8833.GenomeTests.util.WorldManager;
import com.Jeka8833.GenomeTests.world.WorldSimulation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@CommandLine.Command(name = "load", mixinStandardHelpOptions = true)
public class LoadCommand implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger(ViewCommand.class);

    private final WorldManager worldManager;

    @CommandLine.Option(names = "-p", required = true)
    public Path path;

    public LoadCommand(WorldManager worldManager) {
        this.worldManager = worldManager;
    }

    @Override
    public void run() {
        if (Files.isDirectory(path)) {
            try (Stream<Path> walk = Files.walk(path)) {
                walk.filter(path1 -> path1.getFileName().toString().endsWith(".dataworld"))
                        .forEach(path1 -> {
                            try {
                                worldManager.add(FileSaver.loadFromFile(path1, WorldSimulation.class));
                            } catch (IOException | ClassNotFoundException e) {
                                LOGGER.error("Fail load file: " + path1, e);
                            }
                        });
            } catch (IOException e) {
                LOGGER.error("Fail walk", e);
            }
        } else if (Files.isRegularFile(path)) {
            try {
                worldManager.add(FileSaver.loadFromFile(path, WorldSimulation.class));
            } catch (IOException | ClassNotFoundException e) {
                LOGGER.error("Fail load file: " + path, e);
            }
        } else {
            LOGGER.info("Incorrect path");
        }
    }
}
