package com.Jeka8833.GenomeTests.world.visualize;

import com.Jeka8833.GenomeTests.world.World;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.stb.STBEasyFont;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private final Object WORLD_CHANGE_LOCK = new Object();

    private long window = Long.MIN_VALUE;
    private GLCapabilities capabilities;

    private World world;
    private final List<FormLayer> layerManager = new CopyOnWriteArrayList<>();

    private final Vector2f userPosition = new Vector2f();
    private final Vector2i windowsSize = new Vector2i();
    private float zoom = 10;

    private boolean leftPress = false;
    private boolean rightPress = false;
    private boolean upPress = false;
    private boolean downPress = false;

    public Window(World world, FormLayer layerManager) {
        this.world = world;
        this.layerManager.add(layerManager);
    }

    public void init(int posX, int posY, int width, int height) {
        windowsSize.set(width, height);

        window = glfwCreateWindow(width, height, "Empty frame", NULL, NULL);
        if (window == NULL) throw new RuntimeException("Failed to create the GLFW window");

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            switch (key) {
                case GLFW_KEY_ESCAPE -> glfwSetWindowShouldClose(window, true);
                case GLFW_KEY_A -> {
                    if (action == GLFW_PRESS) leftPress = true;
                    else if (action == GLFW_RELEASE) leftPress = false;
                }
                case GLFW_KEY_D -> {
                    if (action == GLFW_PRESS) rightPress = true;
                    else if (action == GLFW_RELEASE) rightPress = false;
                }
                case GLFW_KEY_W -> {
                    if (action == GLFW_PRESS) upPress = true;
                    else if (action == GLFW_RELEASE) upPress = false;
                }
                case GLFW_KEY_S -> {
                    if (action == GLFW_PRESS) downPress = true;
                    else if (action == GLFW_RELEASE) downPress = false;
                }
            }
        });
        glfwSetScrollCallback(window, (windowHandle, xoffset, yoffset) -> {
            float oldZoomWidth = getViewWidth();
            float oldZoomHeight = getViewHeight();

            zoom -= yoffset * (zoom / 3);
            if (zoom < 1) zoom = 1;

            float newZoomWidth = getViewWidth();
            float newZoomHeight = getViewHeight();
            userPosition.add((oldZoomWidth - newZoomWidth) / 2, (oldZoomHeight - newZoomHeight) / 2);
        });
        glfwSetWindowSizeCallback(window, (window1, windowWidth, windowHeight) -> {
            windowsSize.set(windowWidth, windowHeight);

            glfwMakeContextCurrent(window);
            GL.setCapabilities(capabilities);
            glViewport(0, 0, windowWidth, windowHeight);
        });


        glfwMakeContextCurrent(window);
        glfwSwapInterval(0);
        glfwShowWindow(window);

        capabilities = GL.createCapabilities();
        glfwSetWindowPos(window, posX, posY);

        for (FormLayer layer : layerManager) layer.init(this);
    }

    public void tick(double delta) {
        if (isClosed()) {
            for (FormLayer layer : layerManager) layer.close(this);

            glfwFreeCallbacks(window);
            glfwDestroyWindow(window);
            WindowManager.getWindows().remove(this);
            return;
        }

        if (leftPress && !rightPress) userPosition.x -= delta * zoom;
        else if (!leftPress && rightPress) userPosition.x += delta * zoom;

        if (upPress && !downPress) userPosition.y += delta * zoom;
        else if (!upPress && downPress) userPosition.y -= delta * zoom;


        glfwMakeContextCurrent(window);
        GL.setCapabilities(capabilities);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);


        float zoomWidth = getViewWidth();
        float zoomHeight = getViewHeight();
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(userPosition.x(), userPosition.x() + zoomWidth,
                userPosition.y(), userPosition.y() + zoomHeight, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        for (FormLayer layer : layerManager) layer.preRender(this);

        int startX = Math.max(0, (int) userPosition.x());
        int startY = Math.max(0, (int) userPosition.y());
        int stepX = Math.max(1, (int) (zoomWidth / windowsSize.y()));
        int stepY = Math.max(1, (int) (zoomHeight / windowsSize.y()));
        synchronized (WORLD_CHANGE_LOCK) {
            if (world != null) {
                int endX = Math.min(world.getWidth(), (int) Math.ceil(userPosition.x + zoomWidth));
                int endY = Math.min(world.getHeight(), (int) Math.ceil(userPosition.y + zoomHeight));
                for (int y = startY; y < endY; y += stepY) {
                    for (int x = startX; x < endX; x += stepX) {
                        glPushMatrix();
                        glTranslatef(x, y, 0);
                        glScalef(stepX, stepY, 1);

                        for (FormLayer layer : layerManager) layer.cellRender(this, world.getCell(x, y));

                        glPopMatrix();
                    }
                }
            }
        }

        glColor4f(1, 1, 1, 1);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, windowsSize.x(), windowsSize.y(), 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        glPushMatrix();
        glScalef(2, 2, 1f);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        var text = "FPS: " + (int) (1 / delta) + "\nZoom: " + (int) zoom +
                "\nPos X: " + (int) userPosition.x + " Y: " + (int) userPosition.y;
        ByteBuffer charBuffer = BufferUtils.createByteBuffer(text.length() * 270);
        int quads = STBEasyFont.stb_easy_font_print(0, 0, text, null, charBuffer);
        glVertexPointer(2, GL_FLOAT, 16, charBuffer);
        glDrawArrays(GL_QUADS, 0, quads * 4);
        glPopMatrix();

        for (FormLayer layer : layerManager) layer.postRender(this);

        glfwSwapBuffers(window);
    }

    public void addLayer(FormLayer layer) {
        layerManager.add(layer);
    }

    public void removeLayer(FormLayer layer) {
        layerManager.remove(layer);
    }

    public void setWorld(World world) {
        synchronized (WORLD_CHANGE_LOCK) {
            this.world = world;
        }
    }

    public World getWorld() {
        return world;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    public float getZoom() {
        return zoom;
    }

    public float getViewWidth() {
        return windowsSize.x() > windowsSize.y() ? windowsSize.x() * zoom / windowsSize.y() : zoom;
    }

    public float getViewHeight() {
        return windowsSize.x() > windowsSize.y() ? zoom : windowsSize.x() * zoom / windowsSize.y();
    }

    public void setWindowTitle(@NotNull String title) {
        if (window == Long.MIN_VALUE) throw new NullPointerException("The window has not yet been created");

        org.lwjgl.glfw.GLFW.glfwSetWindowTitle(window, title);
    }

    public void close() {
        if (window == Long.MIN_VALUE) throw new NullPointerException("The window has not yet been created");

        glfwSetWindowShouldClose(window, true);
    }

    public boolean isClosed() {
        return glfwWindowShouldClose(window);
    }

    public long getId() {
        return window;
    }

    public Vector2f getUserPosition() {
        return userPosition;
    }

    public Vector2i getWindowsSize() {
        return windowsSize;
    }

}
