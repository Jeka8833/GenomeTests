package com.Jeka8833.GenomeTests.console.console.commands;

import com.Jeka8833.GenomeTests.console.console.Command;
import com.Jeka8833.GenomeTests.console.console.Param;
import com.Jeka8833.GenomeTests.world.WorldTimeManager;

public class TestCommand implements Command {

    @Param(key = "-test")
    private String testField;

    @Param(key = "-test1", need = true, description = "12345")
    private String testField1;

    @Param(key = "-test2", defaultValue = "lol", description = "desc")
    private String testField2;

    @Param(key = {"-c", "-a"},need = true, defaultValue = "lol", description = "desc")
    private String testField3;
    @Param(key = {"-b", "-b"},need = true, defaultValue = "lol", description = "desc")
    private String testField4;
    @Param(key = {"-d", "-c"},need = true, defaultValue = "lol", description = "desc")
    private String testField5;
    @Param(key = {"-a", "-d"},need = true, defaultValue = "lol", description = "desc")
    private String testField6;
    @Param(key = {"-e", "-e"},need = true, defaultValue = "lol", description = "desc")
    private String testField7;
    @Override
    public void execute(String command, WorldTimeManager worldTimeManager) throws Exception {

    }

    @Override
    public String key() {
        return "test";
    }

    @Override
    public String description() {
        return "my description";
    }
}
