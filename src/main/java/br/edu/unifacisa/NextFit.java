package br.edu.unifacisa;

import java.util.LinkedList;

public class NextFit implements AllocationAlgorithm {
    @Override
    public int chooseIndex(LinkedList<MemoryBlock> blocks, int requiredKb, int nextFitStartIndex) {
        if (blocks.isEmpty()) return -1;
        int n = blocks.size();
        int i = nextFitStartIndex;
        for (int k = 0; k < n; k++) {
            MemoryBlock b = blocks.get(i);
            // Procura a partir da última posição usada o primeiro bloco livre que comporte o processo (Next Fit)
            if (b.free && b.sizeKb >= requiredKb) {
                return i;
            }
            i = (i + 1) % n;
        }
        return -1;
    }

    @Override
    public String getName() { return "Next Fit"; }
}