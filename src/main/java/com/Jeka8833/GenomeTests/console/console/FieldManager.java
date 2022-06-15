package com.Jeka8833.GenomeTests.console.console;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class FieldManager {

    private static final Set<Class<? extends Command>> COMMAND_LIST = new HashSet<>();

    public static void registerCommand(Class<? extends Command> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {

            for (ParamAnnotation annotation : field.getAnnotationsByType(ParamAnnotation.class)) {
                System.out.println(annotation);
            }
        }
    }

    public static void main(String[] args) {
        registerCommand(TestCommand.class);
    }

}
