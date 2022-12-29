package com.Jeka8833.GenomeTests.world.visualize;

import com.Jeka8833.GenomeTests.util.FileSaver;
import com.Jeka8833.GenomeTests.world.Cell;
import com.Jeka8833.GenomeTests.world.World;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.lwjgl.glfw.GLFW.*;

public class ReplayOverlay implements FormLayer {

    private int viewedIndex = 0;
    private final Path[] replaysPath;

    public ReplayOverlay(Path replayPath) throws IOException {
        try (Stream<Path> stream = Files.walk(replayPath)) {
            replaysPath = stream.filter(Files::isRegularFile).toArray(Path[]::new);
        }
    }

    @Override
    public void init(Window window) {
        try {
            window.setWorld(FileSaver.loadFromFile(replaysPath[0], World.class));
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        glfwSetKeyCallback(window.getId(), (windowID, key, scancode, action, mods) -> {
            switch (key) {
                case GLFW_KEY_LEFT -> {
                    viewedIndex--;
                    if (viewedIndex < 0) viewedIndex = replaysPath.length - 1;

                    try {
                        window.setWorld(FileSaver.loadFromFile(replaysPath[viewedIndex], World.class));
                    } catch (IOException | ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
                case GLFW_KEY_RIGHT -> {
                    viewedIndex++;
                    if (viewedIndex >= replaysPath.length) viewedIndex = 0;

                    try {
                        window.setWorld(FileSaver.loadFromFile(replaysPath[viewedIndex], World.class));
                    } catch (IOException | ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    @Override
    public void preRender(Window window) {

    }

    @Override
    public void cellRender(Window window, Cell cell) {

    }

    @Override
    public void postRender(Window window) {

    }

    @Override
    public void close(Window window) {

    }
}
