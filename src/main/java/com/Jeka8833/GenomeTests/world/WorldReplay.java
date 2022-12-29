package com.Jeka8833.GenomeTests.world;

import com.Jeka8833.GenomeTests.util.FileSaver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.*;

public class WorldReplay {
    private static final Logger LOGGER = LogManager.getLogger(WorldReplay.class);

    private final ExecutorService writePool;
    private final Path savePath;

    public WorldReplay(Path savePath, int writeThread, int cashSize) throws IOException {
        this.savePath = savePath;
        if (!Files.exists(savePath)) Files.createDirectories(savePath);
        writePool = new ThreadPoolExecutor(0, writeThread,
                10, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(cashSize), Thread.ofVirtual().factory());
    }

    public void snapshotWorld(World world) {
        try {
            final byte[] snapshotWorld = FileSaver.saveToArray(world);
            writePool.execute(() -> {
                try {
                    Files.write(savePath.resolve(world.getName() + "." + world.getTickCount() + ".dataworld"),
                            snapshotWorld);
                } catch (IOException e) {
                    LOGGER.warn("Failure to write a snapshot of the world", e);
                }
            });
        } catch (IOException e) {
            LOGGER.warn("Failure to create a snapshot of the world", e);
        }
    }
}
