package com.Jeka8833.GenomeTests.console.commands;

import com.Jeka8833.GenomeTests.console.console.Command;
import com.Jeka8833.GenomeTests.console.console.CommandException;
import com.Jeka8833.GenomeTests.world.World;
import com.Jeka8833.GenomeTests.world.WorldTimeManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TickCommand implements Command {
    private static final Logger LOGGER = LogManager.getLogger(TickCommand.class);

    @Override
    public void execute(String command, WorldTimeManager worldTimeManager) throws Exception {
        if (command == null) throw new CommandException();
        if (worldTimeManager.getWorlds().isEmpty()) throw new CommandException("World list is empty");

        String[] args = command.split(" ", 2);
        if (args.length <= 1 && args[0].isBlank()) throw new CommandException();

        World world = null;
        if (!args[0].equalsIgnoreCase("all")) {
            world = worldTimeManager.getWorld(args[0]);
            if (world == null) throw new CommandException("Unknown world: " + args[0]);
        }

        int tick = 1;
        if (args.length >= 2) {
            try {
                tick = Integer.parseInt(args[1]);
            } catch (Exception ignored) {
                throw new CommandException("Invalid number(0-100000): " + args[1]);
            }
        }

        if (world == null) {
            for (int i = 0; i < tick; i++) {
                for (World w : worldTimeManager.getWorlds()) {
                    w.tick();
                }
            }
            LOGGER.info("All worlds ticked " + tick + " times");
        } else {
            for (int i = 0; i < tick; i++) {
                world.tick();
            }
            LOGGER.info("World " + world.getName() + " ticked " + tick + " times");
        }
    }

    @Override
    public String prefix() {
        return "tick";
    }

    @Override
    public String help() {
        return "tick <World Name (parameter 'all' to select all worlds) : String> <Tick count (default 1) : int>";
    }
}
