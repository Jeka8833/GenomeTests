package com.Jeka8833.GenomeTests.world;

import java.util.concurrent.CompletableFuture;

public class WorldTime implements Runnable {

    public final World world;
    public CompletableFuture<Void> future;

    public void start() {
        future = CompletableFuture.runAsync(this);
    }

    @Override
    public void run() {

    }
}
