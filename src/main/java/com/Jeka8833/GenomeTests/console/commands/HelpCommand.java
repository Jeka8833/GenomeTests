package com.Jeka8833.GenomeTests.console.commands;

import com.Jeka8833.GenomeTests.console.console.Command;
import com.Jeka8833.GenomeTests.world.WorldTimeManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HelpCommand implements Command {
    private static final Logger LOGGER = LogManager.getLogger(HelpCommand.class);

    @Override
    public void execute(String command, WorldTimeManager worldTimeManager) throws Exception {
        LOGGER.info("Command list:");
        for (Command c : Command.COMMANDS) LOGGER.info(" * " + c.help());
    }

    @Override
    public String prefix() {
        return "help";
    }

    @Override
    public String help() {
        return "help - show list command";
    }
}
