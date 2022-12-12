package com.Jeka8833.GenomeTests.console.commands;

import com.Jeka8833.GenomeTests.console.console.Command;
import com.Jeka8833.GenomeTests.console.console.CommandException;
import com.Jeka8833.GenomeTests.world.World;
import com.Jeka8833.GenomeTests.world.WorldTimeManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SpeedCommand implements Command {
    private static final Logger LOGGER = LogManager.getLogger(SpeedCommand.class);

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

        if (args.length >= 2 && !args[1].isBlank()) {
            try {
                int count = Integer.parseInt(args[1]);
                if (world == null) {
                    for (World w : worldTimeManager.getWorlds())
                        w.setLimitTickPerMinute(count);
                    LOGGER.info("All Worlds ticked " + count + "times");
                } else {
                    world.setLimitTickPerMinute(count);
                    LOGGER.info("World " + world.getName() + " ticked " + count + " time");
                }
            } catch (Exception ignored) {
                throw new CommandException("Invalid number(0-100000): " + args[1]);
            }
        } else {
            LOGGER.info("Speed status:");
            if (world == null) {
                for (World w : worldTimeManager.getWorlds()) {
                    LOGGER.info("* World " + w.getName() + " has speed limit: " +
                            (w.getLimitTickPerMinute() == 0 ? "Unlimited" : w.getLimitTickPerMinute()));
                }
            } else {
                LOGGER.info("* World " + world.getName() + " has speed limit: " +
                        (world.getLimitTickPerMinute() == 0 ? "Unlimited" : world.getLimitTickPerMinute()));
            }
        }
    }

    @Override
    public String key() {
        return "speed";
    }

    @Override
    public String description() {
        return "speed <World Name (parameter 'all' to select all worlds) : String>" +
                " <Tick per minute, set 0 to disable limits " +
                "(But the default value shows you the list of speeds) : int> - set speed to the World(s)";
    }
}
