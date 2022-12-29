package com.Jeka8833.GenomeTests.world.visualize;

import com.Jeka8833.GenomeTests.world.World;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFWErrorCallback;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import static org.lwjgl.glfw.GLFW.*;

public class WindowManager {

    private static final BlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();
    private static final List<Window> windows = new CopyOnWriteArrayList<>();

    private static double lastTickTime = 0;

    static {
        init();
    }

    private static void init() {
        Thread.startVirtualThread(() -> {
            try {
                GLFWErrorCallback.createPrint(System.err).set();
                if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");
                glfwDefaultWindowHints(); // optional, the current window hints are already the default
                glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
                glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
                while (true) {
                    try {
                        if (windows.isEmpty()) {
                            tasks.take().run(); // Wait add new windows
                        } else {
                            Runnable run;
                            while ((run = tasks.poll()) != null) run.run();

                            double time = org.lwjgl.glfw.GLFW.glfwGetTime();
                            double delta = time - lastTickTime;
                            lastTickTime = time;

                            for (Window window : windows) window.tick(delta);
                            glfwPollEvents();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                clearAll();
            }
        });
    }

    public static Window createWindow(World world, FormLayer layerManager) {
        return createWindow(world, layerManager, world == null ? "World" : world.getName(),
                new Vector2i(100, 100), new Vector2i(320, 320));
    }

    public static Window createWindow(World world, FormLayer layerManager, String name,
                                      Vector2i windowPos, Vector2i windowSize) {
        var window = new Window(world, layerManager);
        tasks.add(() -> window.init(windowPos.x(), windowPos.y(), windowSize.x(), windowSize.y()));
        tasks.add(() -> window.setWindowTitle(name));
        windows.add(window);
        return window;
    }

    public static List<Window> getWindows() {
        return windows;
    }

    public static void clearAll() {
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }
}
