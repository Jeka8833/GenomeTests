package com.Jeka8833.GenomeTests.console.commands;

import com.Jeka8833.GenomeTests.console.console.Command;
import com.Jeka8833.GenomeTests.world.World;
import com.Jeka8833.GenomeTests.world.WorldTimeManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ListCommand implements Command {
    private static final Logger LOGGER = LogManager.getLogger(ListCommand.class);

    @Override
    public void execute(String command, WorldTimeManager worldTimeManager) throws Exception {
        if (worldTimeManager.getWorlds().isEmpty()) {
            LOGGER.info("World list is empty");
        } else {
            LOGGER.info("World list:");
            for (World world : worldTimeManager.getWorlds())
                LOGGER.info(" * " + world.getName());
        }
    }

    @Override
    public String prefix() {
        return "list";
    }

    @Override
    public String help() {
        return "list - return world list";
    }
}
