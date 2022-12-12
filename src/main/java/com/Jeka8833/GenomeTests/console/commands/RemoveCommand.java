package com.Jeka8833.GenomeTests.console.commands;

import com.Jeka8833.GenomeTests.console.console.Command;
import com.Jeka8833.GenomeTests.console.console.CommandException;
import com.Jeka8833.GenomeTests.world.WorldTimeManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RemoveCommand implements Command {
    private static final Logger LOGGER = LogManager.getLogger(RemoveCommand.class);


    @Override
    public void execute(String command, WorldTimeManager worldTimeManager) throws Exception {
        if (command == null || command.isBlank()) throw new CommandException();
        if (worldTimeManager.getWorlds().isEmpty()) throw new CommandException("World list is empty");

        if (worldTimeManager.removeWorld(command)) {
            LOGGER.info("World " + command + " removed");
        } else {
            LOGGER.info("Fail remove world " + command);
        }
    }

    @Override
    public String key() {
        return "remove";
    }

    @Override
    public String description() {
        return "remove <World Name : String> - remove world";
    }
}
