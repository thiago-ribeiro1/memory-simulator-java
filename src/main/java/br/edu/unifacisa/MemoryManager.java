package br.edu.unifacisa;

import java.util.*;
import java.util.stream.Collectors;

public class MemoryManager {
    public static final int TOTAL_KB = 128;
    public static final int UNIT_KB = 2; // unidade endereçável
    public static final int UNITS = TOTAL_KB / UNIT_KB; // 64 unidades de 2KB cada

    // Lista encadeada para blocos de memória
    private final LinkedList<MemoryBlock> blocks = new LinkedList<>();
    private AllocationAlgorithm algorithm;
    private int lastNextFitPosition = 0;
    private int steps = 0;

    public MemoryManager(AllocationAlgorithm initialAlgorithm) {
        this.algorithm = initialAlgorithm;
        reset();
    }

    public void setAlgorithm(AllocationAlgorithm algo) {
        this.algorithm = algo;
    }

    /** Resetar memória */
    public void reset() {
        blocks.clear();
        blocks.add(new MemoryBlock(0, TOTAL_KB, true, null));
        lastNextFitPosition = 0;
        steps = 0;
    }

    public int getSteps() { return steps; }

    /** Arredonde para um múltiplo de UNIT_KB */
    public static int align(int kb) {
        if (kb % UNIT_KB == 0) return kb;
        return kb + (UNIT_KB - (kb % UNIT_KB));
    }

    /** Alocar memória contígua para um processo. Retorna verdadeiro se for bem-sucedido */
    public boolean allocate(String processId, int sizeKb) {
        steps++;
        int required = align(sizeKb);
        int index = algorithm.chooseIndex(blocks, required, lastNextFitPosition);
        if (index < 0) return false;
        MemoryBlock b = blocks.get(index);
        if (!b.free || b.sizeKb < required) return false;

        if (b.sizeKb == required) {
            b.free = false;
            b.processId = processId;
        } else {
            // split into [allocated][remainingFree]
            MemoryBlock allocated = new MemoryBlock(b.startKb, required, false, processId);
            MemoryBlock remaining = new MemoryBlock(b.startKb + required, b.sizeKb - required, true, null);
            blocks.set(index, allocated);
            blocks.add(index + 1, remaining);
        }
        lastNextFitPosition = index; // NextFit continua após a posição alocada
        return true;
    }

    /** Libera todos os blocos pertencentes a um ID de processo. Retorna o tamanho liberado */
    public int free(String processId) {
        steps++;
        int releasedKB = 0;
        for (int i = 0; i < blocks.size(); i++) {
            MemoryBlock b = blocks.get(i);
            if (!b.free && Objects.equals(b.processId, processId)) {
                b.free = true;
                b.processId = null;
                releasedKB += b.sizeKb;
                // Tentar mesclar com o anterior
                if (i - 1 >= 0) {
                    MemoryBlock prev = blocks.get(i - 1);
                    if (prev.free) {
                        prev.sizeKb += b.sizeKb;
                        blocks.remove(i);
                        i--; // voltar para a posição atual
                        b = prev;
                    }
                }
                // Tentar mesclar com o próximo
                if (i + 1 < blocks.size()) {
                    MemoryBlock next = blocks.get(i + 1);
                    if (next.free) {
                        b.sizeKb += next.sizeKb;
                        blocks.remove(i + 1);
                    }
                }
            }
        }
        return releasedKB;
    }

    public List<MemoryBlock> snapshotBlocks() {
        return blocks.stream().map(MemoryBlock::copy).collect(Collectors.toList());
    }

    /** Construir uma grade unitária onde -1 = livre, caso contrário mapeia um id do Processo para um índice */
    public int[] snapshotUnitOwners(List<String> processIdOrderOut) {
        Map<String, Integer> processIdIndex = new LinkedHashMap<>();
        processIdOrderOut.clear();
        int[] units = new int[UNITS];
        Arrays.fill(units, -1);
        for (MemoryBlock b : blocks) {
            if (!b.free && b.sizeKb > 0) {
                int idx = processIdIndex.computeIfAbsent(b.processId, k -> {
                    processIdOrderOut.add(k);
                    return processIdOrderOut.size() - 1;
                });
                int startUnit = b.startKb / UNIT_KB;
                int unitCount = b.sizeKb / UNIT_KB;
                for (int u = 0; u < unitCount; u++) {
                    int pos = startUnit + u;
                    if (pos >= 0 && pos < UNITS) {
                        units[pos] = idx;
                    }
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
}