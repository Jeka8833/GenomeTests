package com.Jeka8833.GenomeTests.console.console;

import com.Jeka8833.GenomeTests.console.console.Command;
import com.Jeka8833.GenomeTests.console.console.ParamAnnotation;
import com.Jeka8833.GenomeTests.world.WorldTimeManager;

public class TestCommand implements Command {

    @ParamAnnotation(key = "-test")
    private String testField;

    @Override
    public void execute(String command, WorldTimeManager worldTimeManager) throws Exception {

    }

    @Override
    public String prefix() {
        return "null";
    }

    @Override
    public String help() {
        return "null";
    }
}
