package com.Jeka8833.GenomeTests.world.visualize;

import com.Jeka8833.GenomeTests.world.World;
import org.joml.Vector4i;
import org.lwjgl.glfw.GLFWErrorCallback;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import static org.lwjgl.glfw.GLFW.*;

public class WindowManager {

    private static final BlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();
    private static final List<Window> windows = new CopyOnWriteArrayList<>();

    static {
        init();
    }

    private static void init() {
        var thread = new Thread(() -> {
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

                            for (Window window : windows) window.tick();
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
        thread.setDaemon(true);
        thread.start();
    }

    public static Window createWindow(World world, FormLayerManager layerManager) {
        return createWindow(world, layerManager, world.getName(), new Vector4i(100, 100, 320, 320));
    }

    public static Window createWindow(World world, FormLayerManager layerManager, String name, Vector4i posAndSize) {
        var window = new Window(world, name, layerManager, posAndSize);
        tasks.add(window::init);
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
