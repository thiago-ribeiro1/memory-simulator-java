package br.edu.unifacisa;

public class Operation {
    public enum Type { ALLOC, FREE }
    public final Type type;
    public final String pid;
    public final int sizeKb;

    private Operation(Type type, String pid, int sizeKb) {
        this.type = type;
        this.pid = pid;
        this.sizeKb = sizeKb;
    }

    public static Operation alloc(String pid, int sizeKb) { return new Operation(Type.ALLOC, pid, sizeKb); }
    public static Operation free(String pid) { return new Operation(Type.FREE, pid, 0); }

    @Override
    public String toString() {
        if (type == Type.ALLOC) return "ALLOC " + pid + " " + sizeKb + "KB";
        else return "FREE " + pid;
    }
}