package com.Jeka8833.GenomeTests.console;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.OutputStreamAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.io.IoBuilder;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConsoleHook extends OutputStream {

    private static final int BUFFER_SIZE = 128;
    private static final List<ConsoleListener> OUTPUT_LISTENERS = new ArrayList<>();
    private static final List<ConsoleListener> INPUT_LISTENERS = new ArrayList<>();

    private int count = 0;
    private final byte[] BUFFER = new byte[BUFFER_SIZE];

    @Override
    public void write(int b) {
        BUFFER[count] = (byte) b;
        count++;

        if (count >= BUFFER_SIZE || b == '\n') {
            var text = new String(BUFFER, 0, count, StandardCharsets.UTF_8);
            count = 0;

            for (ConsoleListener listener : OUTPUT_LISTENERS) listener.message(text);
        }
    }

    public static void initConsoleInputHook() {
        Thread.startVirtualThread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String line = scanner.nextLine();
                for (ConsoleListener listener : INPUT_LISTENERS) listener.message(line);
            }
        });
    }


    public static void initHook() {
        System.setOut(
                IoBuilder.forLogger(LogManager.getLogger("system.out"))
                        .setLevel(Level.INFO)
                        .buildPrintStream());
        System.setErr(
                IoBuilder.forLogger(LogManager.getLogger("system.err"))
                        .setLevel(Level.ERROR)
                        .buildPrintStream());

        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        Appender appender = OutputStreamAppender.newBuilder()
                .setTarget(new ConsoleHook())
                .setConfiguration(config)
                .setName("Console Hook")
                .build();
        config.addLoggerAppender(ctx.getRootLogger(), appender);
        appender.start();
    }

    public static void addOutputListener(ConsoleListener listener) {
        OUTPUT_LISTENERS.add(listener);
    }

    public static void addInputListener(ConsoleListener listener) {
        INPUT_LISTENERS.add(listener);
    }

    public static void removeOutputListener(ConsoleListener listener) {
        OUTPUT_LISTENERS.remove(listener);
    }

    public static void removeInputListener(ConsoleListener listener) {
        INPUT_LISTENERS.remove(listener);
    }

    public static void clearOutputListeners() {
        OUTPUT_LISTENERS.clear();
    }

    public static void clearInputListeners() {
        INPUT_LISTENERS.clear();
    }
}
