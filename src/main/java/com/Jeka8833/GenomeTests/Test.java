package com.Jeka8833.GenomeTests;

import com.Jeka8833.GenomeTests.world.World;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Test {
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();
    public static final int WORLD_STOPPED = 0;
    public static final int WORLD_STARTED = 1;
    public static final int WORLD_TRYING_STOP = 2;
    public static final int WORLD_FREEZE = 3;

    private static volatile int worldStatus = 0;

    private static Thread thread;

    public static void main(String[] args) throws InterruptedException {
        Bandwidth bandwidth = Bandwidth.simple(60, Duration.ofMinutes(1)).withInitialTokens(0);

        Bucket bucket = Bucket.builder().addLimit(bandwidth).build();

        System.out.println("Start Time: " + System.currentTimeMillis());
        thread = new Thread(() -> {
            while (worldStatus == WORLD_STARTED) {
                try {
                    bucket.asBlocking().consume(1);

                    System.out.println(System.currentTimeMillis());

                } catch (InterruptedException interruptedException) {
                    break; // Canceling loop
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                System.out.println("Ignore");
            }
            worldStatus = WORLD_STOPPED;
        });
        worldStatus = WORLD_STARTED;
        thread.start();
        threadPool.execute(() -> {
            try {
                Thread.sleep(10000);
                System.out.println("Stop");
                thread.interrupt();
                worldStatus = WORLD_TRYING_STOP;

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println("Iterr: " + waitingEnd(60000));
    }

    public static boolean waitingEnd(long timeout) {
        if (timeout < 0) throw new IllegalArgumentException("Invalid value 'time'");
        long startTime = System.currentTimeMillis();
        while (true) {
            try {
                if (timeout == 0) {
                    thread.join();
                    return true;
                }

                boolean join = thread.join(Duration.ofMillis(timeout));
                return join;
            } catch (IllegalThreadStateException e) {
                return true;
            } catch (InterruptedException e) {
                if (timeout != 0) {
                    timeout -= System.currentTimeMillis() - startTime;
                    if (timeout <= 0) return false;
                }
            }
        }
    }
}
