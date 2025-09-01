package br.edu.unifacisa;

import java.util.*;
import java.util.stream.Collectors;

public class MemoryManager {
    public static final int TOTAL_KB = 128;
    public static final int UNIT_KB = 2;                 // unidade endereçável
    public static final int UNITS = TOTAL_KB / UNIT_KB;  // 64 unidades de 2KB cada

    // Lista encadeada para blocos de memória (ordenados por endereço)
    private final LinkedList<MemoryBlock> blocks = new LinkedList<>();
    private AllocationAlgorithm algorithm;
    private int lastNextFitPosition = 0; // índice onde o Next Fit parou
    private int steps = 0;

    public MemoryManager(AllocationAlgorithm initialAlgorithm) {
        this.algorithm = initialAlgorithm;
        reset();
    }

    public void setAlgorithm(AllocationAlgorithm algo) {
        this.algorithm = algo;
    }

    // Resetar memória para um único bloco livre
    public void reset() {
        blocks.clear();
        blocks.add(new MemoryBlock(0, TOTAL_KB, true, null));
        lastNextFitPosition = 0;
        steps = 0;
    }

    public int getSteps() {
        return steps;
    }

    // Arredondar para múltiplo de UNIT_KB (2KB)
    public static int align(int kb) {
        if (kb % UNIT_KB == 0) return kb;
        return kb + (UNIT_KB - (kb % UNIT_KB));
    }

    // Alocar memória adjacente para um processo, retorna true se alocou
    public boolean allocate(String processId, int sizeKb) {
        steps++;
        int required = align(sizeKb);

        int index = algorithm.chooseIndex(blocks, required, lastNextFitPosition);
        if (index < 0) return false;

        MemoryBlock block = blocks.get(index);
        if (!block.free || block.sizeKb < required) return false;

        if (block.sizeKb == required) {
            allocateExact(block, processId);
        } else {
            allocateSplit(index, block, processId, required);
        }
        lastNextFitPosition = index; // Next Fit continua após a posição alocada
        return true;
    }

    // Libera todos os blocos pertencentes ao processId, retorna total liberado em KB
    public int free(String processId) {
        steps++;
        int releasedKB = 0;

        for (int i = 0; i < blocks.size(); i++) {
            MemoryBlock b = blocks.get(i);
            if (!b.free && Objects.equals(b.processId, processId)) {
                // marca como livre
                b.free = true;
                b.processId = null;
                releasedKB += b.sizeKb;

                // junta com vizinhos livres para reduzir fragmentação
                i = joinAround(i);
            }
        }
        return releasedKB;
    }

    public List<MemoryBlock> snapshotBlocks() {
        return blocks.stream().map(MemoryBlock::copy).collect(Collectors.toList());
    }

    // Mapa por unidade (2KB): -1 livre; >=0 índice do processo na ordem observada (para cores/legenda)
    public int[] snapshotUnitOwners(List<String> processIdOrderOut) {
        Map<String, Integer> processIdIndex = new LinkedHashMap<>();
        processIdOrderOut.clear();

        int[] units = new int[UNITS];
        Arrays.fill(units, -1);

        for (MemoryBlock b : blocks) {
            if (!b.free && b.sizeKb > 0) {
                int pidIdx = processIdIndex.computeIfAbsent(b.processId, k -> {
                    processIdOrderOut.add(k);
                    return processIdOrderOut.size() - 1;
                });
                int startUnit = b.startKb / UNIT_KB;
                int unitCount = b.sizeKb / UNIT_KB;
                for (int u = 0; u < unitCount; u++) {
                    int pos = startUnit + u;
                    if (pos >= 0 && pos < UNITS) units[pos] = pidIdx;
                }
            }
        }
        return units;
    }

    public int usedKb() {
        int used = 0;
        for (MemoryBlock b : blocks) if (!b.free) used += b.sizeKb;
        return used;
    }

    public int freeKb() {
        int free = 0;
        for (MemoryBlock b : blocks) if (b.free) free += b.sizeKb;
        return free;
    }

    // Caso exato: ocupa o bloco inteiro
    private static void allocateExact(MemoryBlock block, String processId) {
        block.free = false;
        block.processId = processId;
    }

    // Caso maior: divide em [alocado][livre restante] mantendo a ordem
    private void allocateSplit(int index, MemoryBlock freeBlock, String processId, int required) {
        MemoryBlock allocated = new MemoryBlock(freeBlock.startKb, required, false, processId);
        MemoryBlock remainder = new MemoryBlock(freeBlock.startKb + required, freeBlock.sizeKb - required, true, null);
        blocks.set(index, allocated);
        blocks.add(index + 1, remainder);
    }

    /**
     * Faz junção no índice informado:
     * - Se vizinho anterior for livre, junta.
     * - Se vizinho seguinte for livre, junta.
     * Retorna o índice do bloco resultante (pode mudar se colou com o anterior).
     */
    private int joinAround(int i) {
        MemoryBlock current = blocks.get(i);

        // Tentar mesclar com o anterior
        if (i - 1 >= 0) {
            MemoryBlock prev = blocks.get(i - 1);
            if (prev.free) {
                prev.sizeKb += current.sizeKb;
                blocks.remove(i);
                i--;                    // o bloco atual agora é o 'prev'
                current = prev;
            }
        }

        // Tentar mesclar com o próximo
        if (i + 1 < blocks.size()) {
            MemoryBlock next = blocks.get(i + 1);
            if (next.free) {
                current.sizeKb += next.sizeKb;
                blocks.remove(i + 1);
            }
        }
        return i;
    }
}
