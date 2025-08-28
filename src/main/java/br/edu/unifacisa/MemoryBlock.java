package br.edu.unifacisa;

public class MemoryBlock {
    public int startKb;     // endereço inicial em KB
    public int sizeKb;      // tamanho do bloco em KB
    public boolean free;    // booleano que define quando o bloco está livre
    public String pid;      // id do processo de memória

    public MemoryBlock(int startKb, int sizeKb, boolean free, String pid) {
        this.startKb = startKb;
        this.sizeKb = sizeKb;
        this.free = free;
        this.pid = pid;
    }

    public MemoryBlock copy() {
        return new MemoryBlock(startKb, sizeKb, free, pid);
    }

    @Override
    public String toString() {
        return String.format("[%s start=%dKB size=%dKB]",
                (free ? "FREE" : "PID " + pid), startKb, sizeKb);
    }
}