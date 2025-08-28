package br.edu.unifacisa;

import java.util.LinkedList;

public class BestFit implements AllocationAlgorithm {

    @Override
    public int chooseIndex(LinkedList<MemoryBlock> blocks, int requiredKb, int nextFitStartIndex) {
        int bestIndex = -1;
        int bestSize = Integer.MAX_VALUE;
        for (int i = 0; i < blocks.size(); i++) {
            MemoryBlock b = blocks.get(i);
            // Verifica cada bloco livre e guarda o menor que seja suficiente para a alocação (Best Fit)
            if (b.free && b.sizeKb >= requiredKb && b.sizeKb < bestSize) {
                bestIndex = i;
                bestSize = b.sizeKb;
            }
        }
        return bestIndex;
    }

    @Override
    public String getName() {
        return "Best Fit";
    }
}