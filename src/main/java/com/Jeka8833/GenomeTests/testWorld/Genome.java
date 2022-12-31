package com.Jeka8833.GenomeTests.testWorld;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

public record Genome(int[] chromosomes, int startIndex) implements Serializable {

    private static final Random RANDOM = new Random();

    public Genome crossing(Genome genome, float percent) {
        return new Genome(
                crossing(chromosomes, genome.chromosomes, percent),
                (startIndex() + genome.startIndex()) / 2);
    }

    public Genome mutation(int bitCount) {
        return new Genome(mutation(chromosomes, bitCount), startIndex());
    }

    public static Genome createGenome(int chromosomesCount) {
        int[] newChromosomes = new int[chromosomesCount];
        for (int i = 0; i < chromosomesCount; i++) newChromosomes[i] = RANDOM.nextInt();

        return new Genome(newChromosomes, RANDOM.nextInt());
    }

    public static int[] crossing(int[] chromosomes1, int[] chromosomes2, float percentFirst) {
        if (chromosomes1.length != chromosomes2.length)
            throw new IllegalArgumentException("Genome 1 length != Genome 2 length");

        int[] newChromosomes = chromosomes2.clone();
        System.arraycopy(chromosomes1, 0, newChromosomes, 0, (int) (percentFirst * chromosomes1.length));
        return newChromosomes;
    }

    public static int[] mutation(int[] chromosomes, int bitCount) {
        int[] newChromosomes = chromosomes.clone();

        for (int i = 0; i < bitCount; i++) {
            int index = RANDOM.nextInt(newChromosomes.length << 5);
            int chromosomeIndex = index >> 5;
            int bit = 1 << (index % 32);

            if ((newChromosomes[chromosomeIndex] & bit) == bit)
                newChromosomes[chromosomeIndex] &= ~bit;
            else
                newChromosomes[chromosomeIndex] |= bit;
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
