package com.Jeka8833.GenomeTests.console.commands;

import com.Jeka8833.GenomeTests.console.console.Command;
import com.Jeka8833.GenomeTests.console.console.CommandException;
import com.Jeka8833.GenomeTests.world.FileSaver;
import com.Jeka8833.GenomeTests.world.World;
import com.Jeka8833.GenomeTests.world.WorldTimeManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class LoadCommand implements Command {
    private static final Logger LOGGER = LogManager.getLogger(LoadCommand.class);

    @Override
    public void execute(String command, WorldTimeManager worldTimeManager) throws Exception {
        if (command == null) throw new CommandException();

        Path path = Path.of(command).toAbsolutePath();
        if (!Files.isRegularFile(path)) throw new CommandException("File not found");

        try {
            List<World> worldList = FileSaver.loadFromFile(path, WorldTimeManager.class).getWorlds();
            for (World world : worldList) addWorld(worldTimeManager, world);
        } catch (Exception ex) {
            try {
                World worldFile = FileSaver.loadFromFile(path, World.class);
                addWorld(worldTimeManager, worldFile);
            } catch (Exception e) {
                throw new CommandException("Fail read file");
            }
        }
    }

    private static void addWorld(WorldTimeManager worldTimeManager, World worldFile) {
        if (worldTimeManager.isWorld(worldFile.getName())) {
            worldTimeManager.removeWorld(worldFile.getName());
            LOGGER.info("World " + worldFile.getName() + " replaced");
        }
        if (worldTimeManager.addWorld(worldFile)) {
            LOGGER.info("Fail add world " + worldFile.getName());
        } else {
            LOGGER.info("World " + worldFile.getName() + " successful added");
        }
    }

    @Override
    public String prefix() {
        return "load";
    }

    @Override
    public String help() {
        return "load <Path to file : String> - load world(s) from file";
    }
}
