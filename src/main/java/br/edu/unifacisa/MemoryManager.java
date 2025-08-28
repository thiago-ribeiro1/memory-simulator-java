package br.edu.unifacisa;

import java.util.*;
import java.util.stream.Collectors;

/** Gerenciador de memória principal. Utiliza uma LinkedList de MemoryBlock representando toda a memória
 * (blocos livres e alocados em ordem de endereço). Agrupam-se em blocos livres */

public class MemoryManager {
    public static final int TOTAL_KB = 128;
    public static final int UNIT_KB = 2; // unidade endereçável
    public static final int UNITS = TOTAL_KB / UNIT_KB; // 64 unidades de 2KB cada

    // Lista encadeada para blocos de memória
    private final LinkedList<MemoryBlock> blocks = new LinkedList<>();
    private AllocationAlgorithm algorithm;
    private int nextFitIndex = 0;
    private int clock = 0;

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
        nextFitIndex = 0;
        clock = 0;
    }

    public int getClock() { return clock; }

    /** Arredonde para um múltiplo de UNIT_KB */
    public static int align(int kb) {
        if (kb % UNIT_KB == 0) return kb;
        return kb + (UNIT_KB - (kb % UNIT_KB));
    }

    /** Alocar memória contígua para um processo. Retorna verdadeiro se for bem-sucedido */
    public boolean allocate(String pid, int sizeKb) {
        clock++;
        int required = align(sizeKb);
        int index = algorithm.chooseIndex(blocks, required, nextFitIndex);
        if (index < 0) return false;
        MemoryBlock b = blocks.get(index);
        if (!b.free || b.sizeKb < required) return false;

        if (b.sizeKb == required) {
            b.free = false;
            b.pid = pid;
        } else {
            // split into [allocated][remainingFree]
            MemoryBlock allocated = new MemoryBlock(b.startKb, required, false, pid);
            MemoryBlock remaining = new MemoryBlock(b.startKb + required, b.sizeKb - required, true, null);
            blocks.set(index, allocated);
            blocks.add(index + 1, remaining);
        }
        nextFitIndex = index; // NextFit continua após a posição alocada
        return true;
    }

    /** Libera todos os blocos pertencentes a um ID de processo. Retorna o tamanho liberado */
    public int free(String pid) {
        clock++;
        int released = 0;
        for (int i = 0; i < blocks.size(); i++) {
            MemoryBlock b = blocks.get(i);
            if (!b.free && Objects.equals(b.pid, pid)) {
                b.free = true;
                b.pid = null;
                released += b.sizeKb;
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
        return released;
    }

    public List<MemoryBlock> snapshotBlocks() {
        return blocks.stream().map(MemoryBlock::copy).collect(Collectors.toList());
    }

    /** Construir uma grade unitária onde -1 = livre, caso contrário mapeia um id do Processo para um índice */
    public int[] snapshotUnitOwners(List<String> pidOrderOut) {
        Map<String, Integer> pidIndex = new LinkedHashMap<>();
        pidOrderOut.clear();
        int[] units = new int[UNITS];
        Arrays.fill(units, -1);
        for (MemoryBlock b : blocks) {
            if (!b.free && b.sizeKb > 0) {
                int idx = pidIndex.computeIfAbsent(b.pid, k -> {
                    pidOrderOut.add(k);
                    return pidOrderOut.size() - 1;
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