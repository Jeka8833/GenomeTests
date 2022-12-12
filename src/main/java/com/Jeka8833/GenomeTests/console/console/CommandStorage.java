package com.Jeka8833.GenomeTests.console.console;

import java.util.Arrays;
import java.util.List;

public record CommandStorage(Class<? extends Command> aClass, String description, List<KeyField> parameters) {

    public boolean containsKey(String key) {
        return parameters.stream()
                .flatMap(keyField -> Arrays.stream(keyField.parameter().key()))
                .anyMatch(s -> s.equalsIgnoreCase(key));
    }

    public KeyField getParameter(String key) {
        for (KeyField keyField : parameters)
            for (String aKey : keyField.parameter().key())
                if (aKey.equalsIgnoreCase(key)) return keyField;
        return null;
    }
}
