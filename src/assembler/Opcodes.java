package assembler;

public enum Opcodes {
    DUP(0x01),
    SWAP(0x02),
    POP(0x03),

    INC(0x04),
    DEC(0x05),

    ADD(0x06),
    SUB(0x07),
    MUL(0x08),

    ADD_STRING_CONST(0x09, 8),


    PUSH_LONG(0x10, 8),

    /**
     * Es wird erwartet, dass X oben ist, und danach kommt y
     */
    MOVE_CURSOR(0xA1,8),

    /**
     * Es wird zuerst posy auf den Stack gepushed und danach posx,
     * sodass x oben liegt
     */
    PUSH_CURSOR_POS_TO_STACK(0xA2),
    SCROLL_TO(0xA3, 4),

    UPDATE_AT(0xB1, 10),
    INSERT_RIGHT_AT(0xB2, 10),
    REMOVE_AT(0xB3, 8),
    PUSH_TO_STACK(0xB4, 8),

    NEW_LINE_AT(0xC1, 4),
    PUSH_MAX_COL_OF_LINE_TO_STACK(0xC2, 4),
    PUSH_MAX_LINES_TO_STACK(0xC3),

    GLOBAL_FUNC(0xD1, 8),
    SET_KEY_MAP(0xD2, 6)
    ;
    private int id;
    private int bytes;
    Opcodes(int id, int bytes) {
        this.id = id;
        this.bytes = bytes;
    }
    Opcodes(int id) {
        this(id, 0);
    }

    public String getId() {
        return String.format("%02X", id);
    }

    public int getBytes() {
        return bytes + 1;
    }
}
