package com.Jeka8833.GenomeTests.console.console.commands;

import com.Jeka8833.GenomeTests.testWorld.FrameManager;
import com.Jeka8833.GenomeTests.util.WorldManager;
import com.Jeka8833.GenomeTests.world.World;
import com.Jeka8833.GenomeTests.world.visualize.FormLayerManager;
import com.Jeka8833.GenomeTests.world.visualize.WindowManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector4i;
import picocli.CommandLine;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@CommandLine.Command(name = "view", mixinStandardHelpOptions = true)
public class ViewCommand implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(ViewCommand.class);

    private static final Map<String, Class<? extends FormLayerManager>> PALETTE = new HashMap<>();

    static {
        addPalette("default", FrameManager.class);
    }

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;
    private final WorldManager worldManager;

    @CommandLine.Option(names = "-w", defaultValue = "all")
    public String[] worlds;
    @CommandLine.Option(names = "-p", defaultValue = "default")
    public String palette;

    public ViewCommand(WorldManager worldManager) {
        this.worldManager = worldManager;
    }

    @Override
    public void run() {
        Class<? extends FormLayerManager> layerManager = PALETTE.get(palette.toLowerCase(Locale.ROOT));
        if (layerManager == null) throw new CommandLine.ParameterException(spec.commandLine(),
                String.format("Invalid value '%s' for option '-p'. Palette list: %s",
                        palette, String.join(", ", PALETTE.keySet())));

        if (worlds.length > 0 && worlds[0].equalsIgnoreCase("all"))
            worlds = worldManager.getWorlds().stream()
                    .map(World::getName)
                    .toArray(String[]::new);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int column = (int) Math.ceil(Math.sqrt(worlds.length));
        int row = (int) Math.floor(Math.sqrt(worlds.length));
        for (int i = 0; i < worlds.length; i++) {
            World world = worldManager.getWorld(worlds[i]);
            if (world == null) {
                LOGGER.warn("World not found '" + worlds[i] + "'");
            } else {
                try {
                    WindowManager.createWindow(world, layerManager.getConstructor().newInstance(), world.getName(),
                            new Vector4i((i % column) * (screenSize.width / column),
                                    31 + ((i / column) % row) * ((screenSize.height - 40) / row),
                                    (screenSize.width / column),
                                    (((screenSize.height - 40 - 31 * row) / row))));
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    LOGGER.warn("Fail create palette", e);
                }
            }
        }
    }

    public static void addPalette(String name, Class<? extends FormLayerManager> palette) {
        PALETTE.put(name, palette);
    }
}
