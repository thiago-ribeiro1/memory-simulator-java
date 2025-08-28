package br.edu.unifacisa;

import java.util.*;

public class Workload {
    private final LinkedList<Operation> ops = new LinkedList<>();
    public void add(Operation op) { ops.add(op); }
    public Operation poll() { return ops.poll(); }
    public boolean isEmpty() { return ops.isEmpty(); }
    public void clear() { ops.clear(); }
    @Override public String toString() { return ops.toString(); }

    /** Cria uma carga de trabalho de demonstração simples */
    public static Workload demo() {
        Workload w = new Workload();
        Random r = new Random(42);
        // Gerar alocações e liberações
        for (int i = 1; i <= 10; i++) {
            int size = (r.nextInt(6) + 1) * 4; // múltiplos de 4 KB entre 4 e 24
            w.add(Operation.alloc("P" + i, size));
            if (i % 3 == 0) { // libera um processo anterior
                int victim = i - 2;
                if (victim > 0) w.add(Operation.free("P" + victim));
            }
        }
        for (int i = 1; i <= 10; i++) w.add(Operation.free("P" + i));
        return w;
    }
}