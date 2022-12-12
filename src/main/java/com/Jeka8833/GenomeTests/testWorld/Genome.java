package com.Jeka8833.GenomeTests.testWorld;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Genome implements Serializable {

    private static final AtomicInteger INDEX_GENERATOR = new AtomicInteger();
    private static final Random RANDOM = new Random();

    public static final Map<Integer, Genome> genomeMap = new HashMap<>();

    private final int id;
    private final int[] chromosomes;

    public Genome(int id, int[] chromosomes) {
        this.id = id;
        this.chromosomes = chromosomes;
    }

    public int[] getChromosomes() {
        return chromosomes;
    }

    public int getId() {
        return id;
    }

    public Genome crossing(Genome genome, float percent) {
        return registerNewGenome(crossing(chromosomes, genome.chromosomes, percent));
    }

    public Genome mutation(float percent) {
        return registerNewGenome(mutation(chromosomes, percent));
    }


    public static int generateIndex() {
        return INDEX_GENERATOR.incrementAndGet();
    }

    public static Genome registerNewGenome(int[] chromosomes) {
        int id = generateIndex();
        var genome = new Genome(id, chromosomes);
        genomeMap.put(id, genome);
        return genome;
    }

    public static Genome createGenome(int chromosomesCount) {
        int[] newChromosomes = new int[chromosomesCount];
        for (int i = 0; i < chromosomesCount; i++)
            newChromosomes[i] = RANDOM.nextInt();

        return registerNewGenome(newChromosomes);
    }

    public static int[] crossing(int[] chromosomes1, int[] chromosomes2, float percentFirst) {
        if (chromosomes1.length != chromosomes2.length)
            throw new IllegalArgumentException("Genome 1 length != Genome 2 length");

        int[] newChromosomes = chromosomes2.clone();
        System.arraycopy(chromosomes1, 0, newChromosomes, 0, (int) (percentFirst * chromosomes1.length));
        return newChromosomes;
    }

    public static int[] mutation(int[] chromosomes, float percent) {
        int[] newChromosomes = chromosomes.clone();

        int mutateInChromosomes = (int) (32 * percent);
        for (int i = 0; i < chromosomes.length; i++) {
            for (int j = 0; j < mutateInChromosomes; j++) {
                int bit = 1 << RANDOM.nextInt(32);
                if ((newChromosomes[i] & bit) == bit)
                    newChromosomes[i] &= ~bit;
                else
                    newChromosomes[i] |= bit;
            }
        }
        return newChromosomes;
    }
}
