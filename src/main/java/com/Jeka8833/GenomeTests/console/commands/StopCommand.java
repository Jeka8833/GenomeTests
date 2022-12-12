package com.Jeka8833.GenomeTests.console.commands;

import com.Jeka8833.GenomeTests.console.console.Command;
import com.Jeka8833.GenomeTests.console.console.CommandException;
import com.Jeka8833.GenomeTests.world.WorldTimeManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StopCommand implements Command {
    private static final Logger LOGGER = LogManager.getLogger(StopCommand.class);

    @Override
    public void execute(String command, WorldTimeManager worldTimeManager) throws Exception {
        if (!worldTimeManager.isRun()) throw new CommandException("World already stopped");

        worldTimeManager.stop();
        LOGGER.info("Worlds is stopped");
    }

    @Override
    public String key() {
        return "stop";
    }

    @Override
    public String description() {
        return "stop - force stop all simulation";
    }
}
