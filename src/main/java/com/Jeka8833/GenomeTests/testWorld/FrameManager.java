package com.Jeka8833.GenomeTests.testWorld;

import com.Jeka8833.GenomeTests.testWorld.objects.Grass;
import com.Jeka8833.GenomeTests.testWorld.objects.Seed;
import com.Jeka8833.GenomeTests.testWorld.objects.Sheet;
import com.Jeka8833.GenomeTests.testWorld.objects.Wood;
import com.Jeka8833.GenomeTests.world.Cell;
import com.Jeka8833.GenomeTests.world.CellLayers;
import com.Jeka8833.GenomeTests.world.visualize.FormLayerManager;
import com.Jeka8833.GenomeTests.world.visualize.Window;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBEasyFont;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_VERTEX_ARRAY;
import static org.lwjgl.opengl.GL11.glEnableClientState;
import static org.lwjgl.opengl.GL15.*;

public class FrameManager implements FormLayerManager {

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
                float zoomWidth = worldFrame.aspect >= 1.0 ? worldFrame.zoom * worldFrame.aspect : worldFrame.zoom;
                float zoomHeight = worldFrame.aspect >= 1.0 ? worldFrame.zoom : worldFrame.zoom * worldFrame.aspect;
                int x = (int) (worldFrame.pos.x + (xBuffer.get(0) * zoomWidth) / worldFrame.getPosAndSize().z());
                int y = (int) (worldFrame.pos.y + ((worldFrame.getPosAndSize().w() - yBuffer.get(0)) * zoomHeight) / worldFrame.getPosAndSize().w());
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
    public void cellRender(Cell cell) {
        if (cell.layers.isEmpty()) return;

        for (CellLayers layer : cell.layers) {
            if (layer instanceof Grass) {
                glColor4f(0.36f, 0.27f, 0.17f, 0.5f);
            } else if (layer instanceof Seed) {
                glColor4f(1, 0, 1, 0.5f);
            } else if (layer instanceof Sheet) {
                glColor4f(0, 1, 0, 0.5f);
            } else if (layer instanceof Wood) {
                glColor4f(1, 0.5f, 0, 0.5f);
            }
        }
        glDrawArrays(GL_QUADS, 0, vertices);
    }

    @Override
    public void postRender(Window worldFrame) {
        if (selectedCell != null) {
            StringBuilder stringBuilder = new StringBuilder();
            if (selectedCell.layers.isEmpty()) return;

            for (CellLayers layer : selectedCell.layers) {
                if (layer instanceof Grass) {
                    stringBuilder.append("Layer: Grass\n");
                } else if (layer instanceof Seed seed) {
                    stringBuilder.append("Layer: Seed\nGenome: ")
                            .append(Arrays.stream(seed.getTreeLive().getGenome().chromosomes())
                                    .mapToObj(Integer::toHexString)
                                    .collect(Collectors.joining(", ")))
                            .append("\nStart Gen: ").append(seed.getStartGen()).append("\nHeath: ")
                            .append(seed.getTreeLive().getHeath()).append("\n");

                } else if (layer instanceof Sheet sheet) {
                    stringBuilder.append("Layer: Sheet\nGenome: ")
                            .append(Arrays.stream(sheet.getTreeLive().getGenome().chromosomes())
                                    .mapToObj(Integer::toHexString)
                                    .collect(Collectors.joining(", ")))
                            .append("\nStart Gen: ").append(sheet.getStartGen()).append("\nHeath: ")
                            .append(sheet.getTreeLive().getHeath()).append("\n");
                } else if (layer instanceof Wood wood) {
                    stringBuilder.append("Layer: Wood\nGenome: ")
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
            ByteBuffer charBuffer = BufferUtils.createByteBuffer(stringBuilder.length() * 270);
            int width = STBEasyFont.stb_easy_font_width(stringBuilder.toString());
            int height = STBEasyFont.stb_easy_font_height(stringBuilder.toString());
            int quads = STBEasyFont.stb_easy_font_print(Math.max(0, worldFrame.getPosAndSize().z() - width),
                    Math.max(0, worldFrame.getPosAndSize().w() - height),
                    stringBuilder.toString(), null, charBuffer);
            glVertexPointer(2, GL_FLOAT, 16, charBuffer);
            glDrawArrays(GL_QUADS, 0, quads * 4);
            glPopMatrix();
        }
    }

    @Override
    public void close() {

    }
}
