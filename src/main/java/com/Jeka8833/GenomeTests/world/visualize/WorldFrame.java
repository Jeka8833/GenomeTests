package com.Jeka8833.GenomeTests.world.visualize;

import com.Jeka8833.GenomeTests.world.World;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.stb.STBEasyFont;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.system.MemoryUtil.NULL;

public class WorldFrame implements Runnable {

    private final World world;
    private FormLayerManager layerManager;

    public final Vector2f pos = new Vector2f();
    public float zoom = 10;
    private boolean leftPress = false;
    private boolean rightPress = false;
    private boolean upPress = false;
    private boolean downPress = false;

    public long window;
    private double lastTime = 0;
    public int width = 300;
    public int height = 300;
    public float aspect = 1;

    public WorldFrame(World world) {
        this(world, null);
    }

    public WorldFrame(World world, FormLayerManager layerManager) {
        this.world = world;
        this.layerManager = layerManager;
    }

    @Override
    public void run() {
        if (layerManager == null) throw new NullPointerException("Layer manager is null");
        int worldWidth = world.getWidth();
        int worldHeight = world.getHeight();

        window = glfwCreateWindow(width, height, world.getName(), NULL, NULL);
        if (window == NULL) throw new RuntimeException("Failed to create the GLFW window");

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            switch (key) {
                case GLFW_KEY_ESCAPE:
                    glfwSetWindowShouldClose(window, true);
                    break;
                case GLFW_KEY_A:
                    if (action == GLFW_PRESS)
                        leftPress = true;
                    else if (action == GLFW_RELEASE)
                        leftPress = false;
                    break;
                case GLFW_KEY_D:
                    if (action == GLFW_PRESS)
                        rightPress = true;
                    else if (action == GLFW_RELEASE)
                        rightPress = false;
                    break;
                case GLFW_KEY_W:
                    if (action == GLFW_PRESS)
                        upPress = true;
                    else if (action == GLFW_RELEASE)
                        upPress = false;
                    break;
                case GLFW_KEY_S:
                    if (action == GLFW_PRESS)
                        downPress = true;
                    else if (action == GLFW_RELEASE)
                        downPress = false;
                    break;
            }
        });
        glfwSetScrollCallback(window, (windowHandle, xoffset, yoffset) -> {
            float oldZoomWidth = aspect >= 1.0 ? zoom * aspect : zoom;
            float oldZoomHeight = aspect >= 1.0 ? zoom : zoom * aspect;
            zoom -= yoffset * (zoom / 3);
            if (zoom < 1)
                zoom = 1;
            float newZoomWidth = aspect >= 1.0 ? zoom * aspect : zoom;
            float newZoomHeight = aspect >= 1.0 ? zoom : zoom * aspect;
            pos.add((oldZoomWidth - newZoomWidth) / 2, (oldZoomHeight - newZoomHeight) / 2);
        });
        glfwSetWindowSizeCallback(window, (window1, width, height) -> {
            aspect = (float) width / height;
            glViewport(0, 0, width, height);
        });


        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        GL.createCapabilities();
        layerManager.init(this);
        while (!glfwWindowShouldClose(window)) {
            double time = GLFW.glfwGetTime();
            float delta = (float) (time - lastTime);
            lastTime = time;

            if (leftPress && !rightPress)
                pos.x -= delta * zoom;
            else if (!leftPress && rightPress)
                pos.x += delta * zoom;

            if (upPress && !downPress)
                pos.y += delta * zoom;
            else if (!upPress && downPress)
                pos.y -= delta * zoom;
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);


            float zoomWidth = aspect >= 1.0 ? zoom * aspect : zoom;
            float zoomHeight = aspect >= 1.0 ? zoom : zoom * aspect;
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            glOrtho(pos.x, pos.x + zoomWidth, pos.y, pos.y + zoomHeight, -1, 1);
            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();

            layerManager.preRender(this);
            int startX = Math.max(0, (int) pos.x);
            int startY = Math.max(0, (int) pos.y);
            int endX = Math.min(worldWidth, (int) Math.ceil(pos.x + zoomWidth));
            int endY = Math.min(worldHeight, (int) Math.ceil(pos.y + zoomHeight));
            int stepX = Math.max(1, (int) (zoomWidth / width));
            int stepY = Math.max(1, (int) (zoomHeight / width));
            for (int y = startY; y < endY; y += stepY) {
                for (int x = startX; x < endX; x += stepX) {
                    glPushMatrix();
                    glTranslatef(x, y, 0);
                    glScalef(stepX, stepY, 1);
                    layerManager.cellRender(world.getCell(x, y));
                    glPopMatrix();
                }
            }
            glColor4f(1, 1, 1, 1);
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            if (aspect >= 1.0)
                glOrtho(0, (width * 0.7f) * aspect, height * 0.7f, 0, -1, 1);
            else
                glOrtho(0, width * 0.7f, (height * 0.7f) / aspect, 0, -1, 1);
            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();

            glBindBuffer(GL_ARRAY_BUFFER, 0);
            var text = "FPS: " + (1 / delta) + "\nZoom: " + zoom + "\nPos X: " + pos.x + " Y: " + pos.y;
            ByteBuffer charBuffer = BufferUtils.createByteBuffer(text.length() * 270);
            int quads = STBEasyFont.stb_easy_font_print(0, 0, text, null, charBuffer);
            glVertexPointer(2, GL_FLOAT, 16, charBuffer);
            glDrawArrays(GL_QUADS, 0, quads * 4);

            layerManager.postRender(this);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
        layerManager.close();
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
    }

    public void setLayerManager(FormLayerManager layerManager) {
        this.layerManager = layerManager;
    }

    public static void init() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
    }

    public static WorldFrame createWindow(World world, FormLayerManager layerManager) {
        var visualize = new WorldFrame(world, layerManager);
        var thread = new Thread(visualize);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.setDaemon(true);
        thread.start();
        return visualize;
    }

    public static void close() {
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }
}