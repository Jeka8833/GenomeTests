//package com.Jeka8833.GenomeTests.console.commands;
//
//import com.Jeka8833.GenomeTests.util.FileSaver;
//import com.Jeka8833.GenomeTests.world.World;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
//import java.nio.file.Files;
//import java.nio.file.Path;
//
//public class SaveCommand implements Command {
//    private static final Logger LOGGER = LogManager.getLogger(SaveCommand.class);
//
//    @Override
//    public void execute(String command, WorldTimeManager worldTimeManager) throws Exception {
//        if (command == null) throw new CommandException();
//        if (worldTimeManager.getWorlds().isEmpty()) throw new CommandException("World list is empty");
//
//        String[] args = command.split(" ", 2);
//        if (args.length <= 1 && args[0].isEmpty()) throw new CommandException();
//
//        Path path = Path.of(args[1]).toAbsolutePath();
//        if (!Files.isDirectory(path.getParent()))
//            Files.createDirectories(path.getParent());
//        if (args[0].equalsIgnoreCase("all")) {
//            FileSaver.saveToFile(path, worldTimeManager);           // Throw exception
//
//            LOGGER.info("Worlds saved to file: " + path);
//        } else {
//            World world = worldTimeManager.getWorld(args[0]);
//            if (world == null) throw new CommandException("Unknown world: " + args[0]);
//            FileSaver.saveToFile(path, world);                      // Throw exception
//
//            LOGGER.info("World saved to file: " + path);
//        }
//    }
//
//    @Override
//    public String key() {
//        return "save";
//    }
//
//    @Override
//    public String description() {
//        return "save <World Name (parameter 'all' to select all worlds) : String> <Path to save : String>" +
//                " - save world(s) to file";
//    }
//}
