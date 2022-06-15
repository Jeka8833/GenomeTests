package com.Jeka8833.GenomeTests.console;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.OutputStreamAppender;
import org.apache.logging.log4j.core.config.Configuration;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConsoleHook extends OutputStream {

    private static final List<ConsoleListener> LISTENERS = new ArrayList<>();
    private static final int BUFFER_SIZE = 128;

    private int count = 0;
    private final byte[] BUFFER = new byte[BUFFER_SIZE];

    @Override
    public void write(int b) throws IOException {
        final byte symbol = (byte) b;
        BUFFER[count] = symbol;
        count++;

        if (count >= BUFFER_SIZE || symbol == '\n') {
            for (ConsoleListener listener : LISTENERS) {
                listener.message(new String(BUFFER, 0, count, StandardCharsets.UTF_8));
                count = 0;
                Arrays.fill(BUFFER, (byte) 0);
            }
        }
    }

    public static void initHook() {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        final Appender appender = OutputStreamAppender.newBuilder()//
                .setTarget(new ConsoleHook())
                .setConfiguration(ctx.getConfiguration())
                .setName("Console Hook")
                .build();
        config.addLoggerAppender(ctx.getRootLogger(), appender);
    }

    public static void addListener(ConsoleListener listener) {
        LISTENERS.add(listener);
    }

    public static void removeListener(ConsoleListener listener) {
        LISTENERS.remove(listener);
    }

    public static void clearListeners() {
        LISTENERS.clear();
    }

}
