package br.edu.unifacisa;

import java.util.LinkedList;

// Interface de estratégia para algoritmos de alocação

public interface AllocationAlgorithm {
    // Escolhe o índice do bloco de memória livre onde será feita a alocação, ou -1 se não couber
    int chooseIndex(LinkedList<MemoryBlock> blocks, int requiredKb, int nextFitStartIndex);

    String getName();
}