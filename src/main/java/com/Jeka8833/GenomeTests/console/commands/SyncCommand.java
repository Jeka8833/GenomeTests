//package com.Jeka8833.GenomeTests.console.commands;
//
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
//public class SyncCommand implements Command {
//    private static final Logger LOGGER = LogManager.getLogger(SyncCommand.class);
//
//
//    @Override
//    public void execute(String command, WorldTimeManager worldTimeManager) throws Exception {
//        if (command == null) throw new CommandException();
//
//        if (!command.equalsIgnoreCase("true") && !command.equalsIgnoreCase("false"))
//            throw new CommandException();
//
//        worldTimeManager.setTickSynchronization(command.equalsIgnoreCase("true"));
//        LOGGER.info("Success");
//    }
//
//    @Override
//    public String key() {
//        return "sync";
//    }
//
//    @Override
//    public String description() {
//        return "sync <true | false> - Enable or disable synchronization";
//    }
//}
