package com.Jeka8833.GenomeTests.console.console;

import java.util.List;

public record CommandStorage(Class<? extends Command> aClass, String description,
                             List<ParametersStorage> parametersList) {
}
