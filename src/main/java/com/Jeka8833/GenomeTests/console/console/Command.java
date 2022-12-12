package com.Jeka8833.GenomeTests.console.console;

import com.Jeka8833.GenomeTests.console.commands.*;
import com.Jeka8833.GenomeTests.world.WorldTimeManager;

public interface Command {

    Command[] COMMANDS = new Command[]{new HelpCommand(), new ListCommand(), new LoadCommand(), new RemoveCommand(),
            new SaveCommand(), new SpeedCommand(), new StartCommand(), new StopCommand(), new SyncCommand(),
            new TickCommand(), new ViewCommand()};

    void execute(String command, WorldTimeManager worldTimeManager) throws Exception;

    String key();

    String description();
}
