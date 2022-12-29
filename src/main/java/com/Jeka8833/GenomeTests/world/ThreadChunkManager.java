package com.Jeka8833.GenomeTests.world;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

public class ThreadChunkManager<V> implements Serializable {

    private int threads = 1;
    private int from = 0;
    private int to = 1;
    private V sendValue;
    private @Nullable Chunk<V> chunkCallback;

    public ThreadChunkManager() {
    }

    public ThreadChunkManager(int from, int to, V sendValue, @NotNull Chunk<V> chunkCallback) {
        this(1, from, to, sendValue, chunkCallback);
    }

    public ThreadChunkManager(int threads, int from, int to, V sendValue, @NotNull Chunk<V> chunkCallback) {
        this.threads = threads;
        this.from = from;
        this.to = to;
        this.sendValue = sendValue;
        this.chunkCallback = chunkCallback;
    }

    public void start() throws InterruptedException {
        if (chunkCallback == null) throw new IllegalArgumentException("Chunk callback is null");
        if (to < from) throw new IllegalArgumentException("to < from");

        int length = to - from;
        int threadParts = Math.min(length, threads);
        if (threadParts == 1) {
            chunkCallback.run(sendValue, from, to);
        } else {
            var threadList = new Thread[threadParts - 1];
            for (int i = 0; i < threadParts; i++) {
                int start = from + (i * length) / threadParts;
                int end = from + ((i + 1) * length) / threadParts;

                if (end == to) {
                    chunkCallback.run(sendValue, start, end);
                } else {
                    threadList[i] = Thread.startVirtualThread(() -> chunkCallback.run(sendValue, start, end));
                }
            }

            for (Thread thread : threadList) thread.join();
        }
    }

    public int getThreads() {
        return threads;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public V getSendValue() {
        return sendValue;
    }

    public @Nullable Chunk<V> getChunkCallback() {
        return chunkCallback;
    }

    public void setThreads(int threads) {
        if (threads < 1) throw new IllegalArgumentException("Incorrect thread count");
        this.threads = threads;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public void setSendValue(V sendValue) {
        this.sendValue = sendValue;
    }

    public void setChunkCallback(@NotNull Chunk<V> chunkCallback) {
        this.chunkCallback = chunkCallback;
    }

    public interface Chunk<V> extends Serializable{
        void run(V value, int from, int to);
    }

}
