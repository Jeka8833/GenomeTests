package com.Jeka8833.GenomeTests.console.console;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;

public record KeyField(Field field, Param parameter) implements Comparator<KeyField> {
    @Override
    public int compare(KeyField o1, KeyField o2) {
        int compare = Boolean.compare(o2.parameter.need(), o1.parameter.need());
        if (compare != 0) return compare;
        return Arrays.compare(o1.parameter.key(), o2.parameter.key());
    }
}
