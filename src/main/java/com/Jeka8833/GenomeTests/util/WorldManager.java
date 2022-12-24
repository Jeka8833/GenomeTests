package com.Jeka8833.GenomeTests.util;

import com.Jeka8833.GenomeTests.world.World;
import com.Jeka8833.GenomeTests.world.WorldSimulation;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class WorldManager implements Serializable {

    private final List<WorldSimulation> worldSimulations = new CopyOnWriteArrayList<>();

    public void add(WorldSimulation worldSimulation) {
        worldSimulations.add(worldSimulation);
    }

    public WorldSimulation add(World world) {
        var simulation = new WorldSimulation(world);
        worldSimulations.add(simulation);
        return simulation;
    }

    public void remove(WorldSimulation worldSimulation) {
        worldSimulations.remove(worldSimulation);
    }

    public void remove(World world) {
        worldSimulations.removeIf(worldSimulation -> worldSimulation.getWorld().equals(world));
    }

    public void remove(String name) {
        worldSimulations.removeIf(worldSimulation -> worldSimulation.getWorld().getName().equalsIgnoreCase(name));
    }

    public WorldSimulation getSimulation(World world) {
        return worldSimulations.stream()
                .filter(worldSimulation -> worldSimulation.getWorld().equals(world))
                .findFirst().orElse(null);
    }

    public WorldSimulation getSimulation(String name) {
        return worldSimulations.stream()
                .filter(worldSimulation -> worldSimulation.getWorld().getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    public World getWorld(String name) {
        return worldSimulations.stream()
                .map(WorldSimulation::getWorld)
                .filter(world -> world.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    public List<WorldSimulation> getWorldSimulations() {
        return worldSimulations;
    }

    public List<World> getWorlds() {
        return worldSimulations.stream()
                .map(WorldSimulation::getWorld)
                .toList();
    }


}
