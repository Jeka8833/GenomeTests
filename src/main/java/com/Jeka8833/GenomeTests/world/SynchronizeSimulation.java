package com.Jeka8833.GenomeTests.world;

import io.github.bucket4j.Bucket;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SynchronizeSimulation implements Serializable {

    private transient final List<WorldSimulation> simulations = new ArrayList<>();
    private transient Bucket bestRateLimitWorld;

    private transient final AtomicInteger threadsReady = new AtomicInteger();

    private transient volatile int needReady = 0;

    private transient final Object LOCK = new Object();


    public void registerWorld(WorldSimulation simulation) {
        simulations.add(simulation);
        recalculateSynchronize();
    }

    public void unregisterWorld(WorldSimulation simulation) {
        simulations.remove(simulation);
        recalculateSynchronize();
    }

    public void tick() throws InterruptedException {
        if (threadsReady.incrementAndGet() >= needReady) {
            LOCK.notifyAll();
        } else {
            LOCK.wait(30_000);
            if (threadsReady.get() < needReady) LOCK.notifyAll();
        }

        if (bestRateLimitWorld != null) bestRateLimitWorld.asBlocking().consume(1);
    }

    public void recalculateSynchronize() {
        int needReadyCalculated = (int) simulations.stream()
                .filter(WorldSimulation::isRun)
                .count();
        bestRateLimitWorld = simulations.stream()
                .filter(simulation -> simulation.getLimitTickPerMinute() > 0)
                .max(Comparator.comparingInt(WorldSimulation::getLimitTickPerMinute))
                .map(WorldSimulation::getRateLimit)
                .orElse(null);

        // Fast replacement block
        needReady = needReadyCalculated;
        threadsReady.set(0);
        LOCK.notifyAll();
    }
}
