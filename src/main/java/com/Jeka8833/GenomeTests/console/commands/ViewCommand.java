package com.Jeka8833.GenomeTests.console.commands;

import com.Jeka8833.GenomeTests.console.console.Command;
import com.Jeka8833.GenomeTests.console.console.CommandException;
import com.Jeka8833.GenomeTests.testWorld.FrameManager;
import com.Jeka8833.GenomeTests.world.World;
import com.Jeka8833.GenomeTests.world.WorldTimeManager;
import com.Jeka8833.GenomeTests.world.visualize.FormLayerManager;
import com.Jeka8833.GenomeTests.world.visualize.WorldFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class ViewCommand implements Command {
    private static final Logger LOGGER = LogManager.getLogger(ViewCommand.class);

    private static final Map<String, FormLayerManager> PALETTE = new HashMap<>();

    static {
        PALETTE.put("default", new FrameManager());
    }

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

        FormLayerManager layerManager = new FrameManager();

        if (args.length >= 2 && !args[1].isBlank()) {
            FormLayerManager find = PALETTE.get(args[1].toLowerCase());
            if (find == null) {
                LOGGER.info("Fail find palette, palette list:");
                for (Map.Entry<String, FormLayerManager> entry : PALETTE.entrySet())
                    LOGGER.info(" - " + entry.getKey() + "(" + entry.getValue().getClass().getCanonicalName() + ")");
            } else {
                layerManager = find;
            }
        }


        if (world == null) {
            for (World w : worldTimeManager.getWorlds()) {
                WorldFrame.createWindow(w, layerManager.getClass().getConstructor().newInstance());
            }
        } else {
            WorldFrame.createWindow(world, layerManager);
        }
    }

    private static FormLayerManager getPalette(String name) {
        return PALETTE.get(name.toLowerCase());
    }

    @Override
    public String prefix() {
        return "view";
    }

    @Override
    public String help() {
        return "view <World Name (parameter 'all' to select all worlds) : String> " +
                "<Palette (Default palette is 'FrameManager') : String> - open simulation window";
    }
}
