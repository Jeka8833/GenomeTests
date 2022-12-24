package com.Jeka8833.GenomeTests.testWorld;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

public record Genome(int[] chromosomes) implements Serializable {

    private static final Random RANDOM = new Random();

    public Genome crossing(Genome genome, float percent) {
        return new Genome(crossing(chromosomes, genome.chromosomes, percent));
    }

    public Genome mutation(float percent) {
        return new Genome(mutation(chromosomes, percent));
    }

    public static Genome createGenome(int chromosomesCount) {
        int[] newChromosomes = new int[chromosomesCount];
        for (int i = 0; i < chromosomesCount; i++)
            newChromosomes[i] = RANDOM.nextInt();

        return new Genome(newChromosomes);
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

    @Override
    public String toString() {
        return "Genome{" +
                "chromosomes=[" + Arrays.stream(chromosomes).mapToObj(Integer::toHexString).collect(Collectors.joining(", ")) +
                "]}";
    }
}
