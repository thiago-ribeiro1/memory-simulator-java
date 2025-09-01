package br.edu.unifacisa;

import java.util.LinkedList;

public class FirstFit implements AllocationAlgorithm {

    @Override
    public int chooseIndex(LinkedList<MemoryBlock> blocks, int requiredKb, int nextFitStartIndex) {
        for (int i = 0; i < blocks.size(); i++) {
            MemoryBlock b = blocks.get(i);
            // Retorna o primeiro bloco livre com tamanho suficiente para a alocação (First Fit)
            if (b.free && b.sizeKb >= requiredKb) return i;
        }
        return -1; // não encontrou bloco livre suficiente na memória
    }

    @Override
    public String getName() { return "First Fit"; }
}