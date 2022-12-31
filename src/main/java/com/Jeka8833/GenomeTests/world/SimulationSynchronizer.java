package com.Jeka8833.GenomeTests.world;

import io.github.bucket4j.Bucket;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SimulationSynchronizer implements Serializable {
    private transient final List<WorldSimulation> simulations = new ArrayList<>();
    private transient final Object LOCK = new Object();
    private transient Bucket worldSpeed;

    private transient volatile int readyThreads = 0;
    private transient volatile int needReady = 0;

    public void registerWorld(WorldSimulation simulation) {
        simulations.add(simulation);
        recalculateSynchronize();
    }

    public void unregisterWorld(WorldSimulation simulation) {
        simulations.remove(simulation);
        recalculateSynchronize();
    }

    public void tick() throws InterruptedException {
        synchronized (LOCK) {
            readyThreads++;
            if (readyThreads >= needReady) {
                readyThreads = 0;
                LOCK.notifyAll();
            } else {
                LOCK.wait(30_000);
            }
        }

        if (worldSpeed != null) worldSpeed.asBlocking().consume(1);
    }

    public void recalculateSynchronize() {
        int needReadyCalculated = (int) simulations.stream()
                .filter(WorldSimulation::isRun)
                .count();
        worldSpeed = simulations.stream()
                .filter(simulation -> simulation.getLimitTickPerMinute() > 0)
                .max(Comparator.comparingInt(WorldSimulation::getLimitTickPerMinute))
                .map(WorldSimulation::getRateLimit)
                .orElse(null);

        // Fast execution block
        synchronized (LOCK) {
            needReady = needReadyCalculated;
            readyThreads = 0;
            LOCK.notifyAll();
        }
    }
}
