package com.Jeka8833.GenomeTests.console.console;

import com.Jeka8833.GenomeTests.console.console.commands.TestCommand;
import com.Jeka8833.GenomeTests.world.WorldTimeManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class FieldManager {
    private static final Logger LOGGER = LogManager.getLogger(FieldManager.class);
    private static final Map<String, CommandStorage> COMMAND_LIST = new HashMap<>();

    public static void registerCommand(Class<? extends Command> clazz)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        /*Command abstractCommand = clazz.getConstructor().newInstance();
        List<KeyField> fields = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()){
            if()
        }


        COMMAND_LIST.put(abstractCommand.key(),
                new CommandStorage(clazz, abstractCommand.description(),
                        Arrays.stream(clazz.getDeclaredFields())
                                .map(field -> field.getAnnotation(Param.class))
                                .collect(Collectors.toList())
                ));*/
    }

    public static void runCommand(String line, WorldTimeManager timeManager)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        List<String> parameters = splitParameters(line);

        if (parameters.isEmpty()) throw new CommandException("Empty values");
        Iterator<String> iterator = parameters.listIterator();
        CommandStorage commandStructure = COMMAND_LIST.get(iterator.next());
        Command newCommand = commandStructure.aClass().getConstructor().newInstance();
        while (iterator.hasNext()) {
            String key = iterator.next().toLowerCase();
            if (!commandStructure.containsKey(key)) continue;

            if (!iterator.hasNext()) break;
            String value = iterator.next();

            //newCommand.getClass().getField()
        }
    }

    private static List<String> splitParameters(String text) {
        List<String> parameters = new ArrayList<>();
        StringBuilder stringBuffer = new StringBuilder();
        String[] spaceSplit = text.split(" ");
        boolean isString = false;

        for (String fragment : spaceSplit) {
            if (!isString && fragment.startsWith("\"")) {
                stringBuffer.append(fragment).append(' ');
                isString = true;
            } else if (isString && fragment.endsWith("\"")) {
                stringBuffer.append(fragment);
                parameters.add(String.join(" ", stringBuffer.substring(1, stringBuffer.length() - 1)));
                stringBuffer.setLength(0); // Clear buffer
                isString = false;
            } else {
                if (isString) {
                    stringBuffer.append(fragment);
                } else {
                    parameters.add(fragment);
                }
            }
        }
        return parameters;
    }

    public static void printHelp() {
        if (COMMAND_LIST.isEmpty()) {
            LOGGER.warn("Command list is empty");
        } else {
            LOGGER.info("Command list(If need help, then use the -h or -help parameter): ");
            for (Map.Entry<String, CommandStorage> entry : COMMAND_LIST.entrySet()) {
                LOGGER.info(" > " + entry.getKey() + " - " + entry.getValue().description());
            }
        }
    }

    public static void printHelp(String command) {
        /*String clearedCommand = command.trim().toLowerCase();
        CommandStorage currentCommand = COMMAND_LIST.get(clearedCommand);
        if (currentCommand == null) {
            LOGGER.warn("This command is not exist");
        } else {
            if (currentCommand.annotations().isEmpty()) {
                LOGGER.info("Command don't have parameters");
            } else {
                LOGGER.info("Help for '" + clearedCommand + "' command:");
                currentCommand.annotations().stream()
                        .sorted(new AnnotationComparator())
                        .forEach(annotation -> {
                            var sb = new StringBuffer(" > " + String.join(", ", annotation.key()));

                            if (!annotation.defaultValue().isBlank())
                                sb.append("(Default: ").append(annotation.defaultValue()).append(")");
                            if (annotation.need())
                                sb.append(" <Need>");
                            if (!annotation.description().isBlank())
                                sb.append(" - ").append(annotation.description());
                            LOGGER.info(sb);
                        });
            }
        }*/
    }

    public static void main(String[] args) {
        try {
            registerCommand(TestCommand.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        printHelp();
        printHelp("test");
    }

}
