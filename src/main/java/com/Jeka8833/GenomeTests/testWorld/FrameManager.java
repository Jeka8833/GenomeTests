package com.Jeka8833.GenomeTests.testWorld;

import com.Jeka8833.GenomeTests.testWorld.objects.Grass;
import com.Jeka8833.GenomeTests.testWorld.objects.Seed;
import com.Jeka8833.GenomeTests.testWorld.objects.Sheet;
import com.Jeka8833.GenomeTests.testWorld.objects.Wood;
import com.Jeka8833.GenomeTests.world.Cell;
import com.Jeka8833.GenomeTests.world.Layer;
import com.Jeka8833.GenomeTests.world.visualize.FormLayer;
import com.Jeka8833.GenomeTests.world.visualize.Window;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBEasyFont;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL15.*;

public class FrameManager implements FormLayer {

    private static final int vertices = 4;
    private static final int vertex_size = 2;

    private int vbo_vertex_handle;

    private Cell selectedCell = null;

    @Override
    public void init(Window worldFrame) {
        glfwSetMouseButtonCallback(worldFrame.getId(), (windowHnd, button, action, mods) -> {
            if (action == GLFW_RELEASE && button == GLFW_MOUSE_BUTTON_1) {
                DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
                DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);
                glfwGetCursorPos(worldFrame.getId(), xBuffer, yBuffer);

                float aspect = (float) worldFrame.getWindowsSize().x() / worldFrame.getWindowsSize().y();
                float zoomWidth = aspect >= 1.0 ? worldFrame.getZoom() * aspect : worldFrame.getZoom();
                float zoomHeight = aspect >= 1.0 ? worldFrame.getZoom() : worldFrame.getZoom() * aspect;
                int x = (int) (worldFrame.getUserPosition().x() + (xBuffer.get(0) * zoomWidth) / worldFrame.getWindowsSize().x());
                int y = (int) (worldFrame.getUserPosition().y() +
                        ((worldFrame.getWindowsSize().y() - yBuffer.get(0)) * zoomHeight) /
                                worldFrame.getWindowsSize().y());
                selectedCell = worldFrame.getWorld().getCell(x, y);
            }
        });

        FloatBuffer vertex_data = BufferUtils.createFloatBuffer(vertices * vertex_size);
        vertex_data.put(new float[]{0, 0});
        vertex_data.put(new float[]{1, 0});
        vertex_data.put(new float[]{1, 1});
        vertex_data.put(new float[]{0, 1});
        vertex_data.flip();

        vbo_vertex_handle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo_vertex_handle);
        glBufferData(GL_ARRAY_BUFFER, vertex_data, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glEnableClientState(GL_VERTEX_ARRAY);
    }

    @Override
    public void preRender(Window worldFrame) {
        glBindBuffer(GL_ARRAY_BUFFER, vbo_vertex_handle);
        glVertexPointer(vertex_size, GL_FLOAT, 0, 0);
    }

    @Override
    public void cellRender(Window window, Cell cell) {
        for (Layer layer : cell.layers.toArray(Layer[]::new)) {
            switch (layer) {
                case Grass grass -> glColor4f(0.36f, 0.27f, 0.17f, grass.getColorBrightness());
                case Seed seed -> glColor4f(1, 0, 1, 0.5f);
                case Sheet sheet -> glColor4f(0, 1, 0, 0.5f);
                case Wood wood -> glColor4f(1, 0.5f, 0, 0.5f);
                case null, default -> {
                    continue;
                }
            }
            glDrawArrays(GL_QUADS, 0, vertices);
        }
    }

    @Override
    public void postRender(Window worldFrame) {
        StringBuilder worldInfo = new StringBuilder();
        worldInfo.append("Sun level: ").append(SimpleWorldGenerator.getSunLevel(worldFrame.getWorld().getTickCount()))
                .append("\nTick: ").append(worldFrame.getWorld().getTickCount());

        glPushMatrix();
        // glScalef(2, 2, 1f);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        ByteBuffer charBuffer = BufferUtils.createByteBuffer(worldInfo.length() * 270);
        int width = STBEasyFont.stb_easy_font_width(worldInfo.toString());
        int quads = STBEasyFont.stb_easy_font_print(Math.max(0, worldFrame.getWindowsSize().x() - width),
                0,
                worldInfo.toString(), null, charBuffer);
        glVertexPointer(2, GL_FLOAT, 16, charBuffer);
        glDrawArrays(GL_QUADS, 0, quads * 4);
        glPopMatrix();


        if (selectedCell != null) {
            StringBuilder cellInfo = new StringBuilder();
            if (selectedCell.layers.isEmpty()) return;
            cellInfo.append("Cell pos: ").append(selectedCell.x).append(" ").append(selectedCell.y).append("\n");
            for (Layer layer : selectedCell.layers) {
                if (layer instanceof Grass grass) {
                    cellInfo.append("Layer: Grass\nEnergy: ").append(grass.getEnergy()).append("\n");
                } else if (layer instanceof Seed seed) {
                    cellInfo.append("Layer: Seed\nGenome: ")
                            .append(Arrays.stream(seed.getTreeLive().getGenome().chromosomes())
                                    .mapToObj(Integer::toHexString)
                                    .collect(Collectors.joining(", ")))
                            .append("\nStart Gen: ").append(seed.getStartGen()).append("\nHeath: ")
                            .append(seed.getTreeLive().getHeath()).append("\n");

                } else if (layer instanceof Sheet sheet) {
                    cellInfo.append("Layer: Sheet\nGenome: ")
                            .append(Arrays.stream(sheet.getTreeLive().getGenome().chromosomes())
                                    .mapToObj(Integer::toHexString)
                                    .collect(Collectors.joining(", ")))
                            .append("\nStart Gen: ").append(sheet.getStartGen()).append("\nHeath: ")
                            .append(sheet.getTreeLive().getHeath()).append("\n");
                } else if (layer instanceof Wood wood) {
                    cellInfo.append("Layer: Wood\nGenome: ")
                            .append(Arrays.stream(wood.getTreeLive().getGenome().chromosomes())
                                    .mapToObj(Integer::toHexString)
                                    .collect(Collectors.joining(", ")))
                            .append("\nStart Gen: ").append(wood.getStartGen()).append("\nHeath: ")
                            .append(wood.getTreeLive().getHeath()).append("\n");
                }
            }

            glPushMatrix();
            // glScalef(2, 2, 1f);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            ByteBuffer charBuffer1 = BufferUtils.createByteBuffer(cellInfo.length() * 270);
            int width1 = STBEasyFont.stb_easy_font_width(cellInfo.toString());
            int height1 = STBEasyFont.stb_easy_font_height(cellInfo.toString());
            int quads1 = STBEasyFont.stb_easy_font_print(Math.max(0, worldFrame.getWindowsSize().x() - width1),
                    Math.max(0, worldFrame.getWindowsSize().y() - height1),
                    cellInfo.toString(), null, charBuffer1);
            glVertexPointer(2, GL_FLOAT, 16, charBuffer1);
            glDrawArrays(GL_QUADS, 0, quads1 * 4);
            glPopMatrix();
        }
    }

    @Override
    public void close(Window window) {

    }
}
