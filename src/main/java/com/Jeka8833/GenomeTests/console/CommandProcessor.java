package com.Jeka8833.GenomeTests.console;

import com.Jeka8833.GenomeTests.console.commands.*;
import com.Jeka8833.GenomeTests.util.WorldManager;
import picocli.CommandLine;

@CommandLine.Command()
public class CommandProcessor {


    public static void main(String[] args) {
        process(null, "view");
    }

    public static void process(WorldManager worldManager, String... args) {
        new CommandLine(new CommandProcessor())
                .addSubcommand(new ViewCommand(worldManager))
                .addSubcommand(new StartCommand(worldManager))
                .addSubcommand(new StopCommand(worldManager))
                .addSubcommand(new SaveCommand(worldManager))
                .addSubcommand(new LoadCommand(worldManager))
                .addSubcommand(new TickCommand(worldManager))
                .addSubcommand(new RestartCommand(worldManager))
                .addSubcommand(new RemoveCommand(worldManager))
                .execute(args.length == 1 ? args[0].split(" ") : args);
    }

}
