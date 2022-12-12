package com.Jeka8833.GenomeTests.console.commands;

import com.Jeka8833.GenomeTests.console.console.Command;
import com.Jeka8833.GenomeTests.console.console.CommandException;
import com.Jeka8833.GenomeTests.world.WorldTimeManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StartCommand implements Command {
    private static final Logger LOGGER = LogManager.getLogger(StartCommand.class);

    @Override
    public void execute(String command, WorldTimeManager worldTimeManager) throws Exception {
        if (worldTimeManager.isRun()) throw new CommandException("World already started");
        if (worldTimeManager.getWorlds().isEmpty()) throw new CommandException("World list is empty");

        worldTimeManager.start();
        LOGGER.info("Worlds is started");
    }

    @Override
    public String key() {
        return "start";
    }

    @Override
    public String description() {
        return "start - Run all simulations";
    }
}
