package br.edu.unifacisa;

public class Operation {
    public enum Type { ALLOC, FREE }
    public final Type type;
    public final String processId;
    public final int sizeKb;

    private Operation(Type type, String processId, int sizeKb) {
        this.type = type;
        this.processId = processId;
        this.sizeKb = sizeKb;
    }

    public static Operation alloc(String processId, int sizeKb) { return new Operation(Type.ALLOC, processId, sizeKb); }
    public static Operation free(String processId) { return new Operation(Type.FREE, processId, 0); }

    @Override
    public String toString() {
        if (type == Type.ALLOC) return "ALLOC " + processId + " " + sizeKb + "KB";
        else return "FREE " + processId;
    }
}