package com.Jeka8833.GenomeTests.world.visualize;

import com.Jeka8833.GenomeTests.world.World;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector4i;
import org.joml.Vector4ic;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.stb.STBEasyFont;

import java.nio.ByteBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    private long window;
    private GLCapabilities capabilities;

    private final Object LOCK = new Object();

    private @NotNull World world;

    private final @NotNull String name;
    private final @NotNull FormLayerManager layerManager;
    private final @NotNull Vector4i posAndSize;

    private volatile boolean forceStop = false;

    public final Vector2f pos = new Vector2f();

    public float zoom = 10;

    private boolean leftPress = false;

    private boolean rightPress = false;
    private boolean upPress = false;
    private boolean downPress = false;
    public float aspect = 1;
    private double lastTime = 0;

    public Window(@NotNull World world, @NotNull String name,
                  @NotNull FormLayerManager layerManager, @NotNull Vector4i posAndSize) {
        this.world = world;
        this.name = name;
        this.layerManager = layerManager;
        this.posAndSize = posAndSize;
    }

    public void init() {
        int worldWidth = world.getWidth();
        int worldHeight = world.getHeight();

        window = glfwCreateWindow(posAndSize.z(), posAndSize.w(), name, NULL, NULL);
        if (window == NULL) throw new RuntimeException("Failed to create the GLFW window");

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            switch (key) {
                case GLFW_KEY_ESCAPE -> glfwSetWindowShouldClose(window, true);
                case GLFW_KEY_A -> {
                    if (action == GLFW_PRESS)
                        leftPress = true;
                    else if (action == GLFW_RELEASE)
                        leftPress = false;
                }
                case GLFW_KEY_D -> {
                    if (action == GLFW_PRESS)
                        rightPress = true;
                    else if (action == GLFW_RELEASE)
                        rightPress = false;
                }
                case GLFW_KEY_W -> {
                    if (action == GLFW_PRESS)
                        upPress = true;
                    else if (action == GLFW_RELEASE)
                        upPress = false;
                }
                case GLFW_KEY_S -> {
                    if (action == GLFW_PRESS)
                        downPress = true;
                    else if (action == GLFW_RELEASE)
                        downPress = false;
                }
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
            posAndSize.z = width;
            posAndSize.w = height;
            aspect = (float) width / height;
            glViewport(0, 0, width, height);
        });


        glfwMakeContextCurrent(window);
        glfwSwapInterval(0);
        glfwShowWindow(window);

        capabilities = GL.createCapabilities();
        glfwSetWindowPos(window, posAndSize.x(), posAndSize.y());
        layerManager.init(this);
    }

    public void tick() {
        if (forceStop || glfwWindowShouldClose(window)) {
            layerManager.close();
            glfwFreeCallbacks(window);
            glfwDestroyWindow(window);
            WindowManager.getWindows().remove(this);
            return;
        }

        glfwMakeContextCurrent(window);
        GL.setCapabilities(capabilities);

        double time = org.lwjgl.glfw.GLFW.glfwGetTime();
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
        int stepX = Math.max(1, (int) (zoomWidth / posAndSize.z()));
        int stepY = Math.max(1, (int) (zoomHeight / posAndSize.z()));
        synchronized (LOCK) {
            int endX = Math.min(world.getWidth(), (int) Math.ceil(pos.x + zoomWidth));
            int endY = Math.min(world.getHeight(), (int) Math.ceil(pos.y + zoomHeight));
            for (int y = startY; y < endY; y += stepY) {
                for (int x = startX; x < endX; x += stepX) {
                    glPushMatrix();
                    glTranslatef(x, y, 0);
                    glScalef(stepX, stepY, 1);
                    layerManager.cellRender(world.getCell(x, y));
                    glPopMatrix();
                }
            }
        }
        glColor4f(1, 1, 1, 1);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, posAndSize.z(), posAndSize.w(), 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();


        glPushMatrix();
        glScalef(2, 2, 1f);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        var text = "FPS: " + (1 / delta) + "\nZoom: " + zoom + "\nPos X: " + pos.x + " Y: " + pos.y;
        ByteBuffer charBuffer = BufferUtils.createByteBuffer(text.length() * 270);
        int quads = STBEasyFont.stb_easy_font_print(0, 0, text, null, charBuffer);
        glVertexPointer(2, GL_FLOAT, 16, charBuffer);
        glDrawArrays(GL_QUADS, 0, quads * 4);
        glPopMatrix();


        layerManager.postRender(this);

        glfwSwapBuffers(window);
    }

    public Vector4i getPosAndSize() {
        return posAndSize;
    }

    public void setWorld(World world) {
        synchronized (LOCK) {
            this.world = world;
        }
    }

    public World getWorld() {
        return world;
    }

    public void close() {
        forceStop = true;
    }

    public long getId() {
        return window;
    }
}
