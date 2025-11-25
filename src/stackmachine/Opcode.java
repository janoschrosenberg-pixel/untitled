package stackmachine;

public enum Opcode {
    WRITE_CODE,
    SWAP,
    DUP,
    LOAD,
    BIND,
    ADD,
    MUL,
    SUB,
    DIV,
    PRINT,
    PUSH_STRING,
    PUSH_CHAR,
    PUSH_DOUBLE,
    CALL;

    public static boolean matches(String s) {
        return fromString(s) != null;
    }

    public static Opcode fromString(String s) {
        if (s == null) return null;

        String normalized = s.trim().toUpperCase();

        for (Opcode ct : values()) {
            if (ct.name().equalsIgnoreCase(normalized)) {
                return ct;
            }
        }
        return null;
    }
}
