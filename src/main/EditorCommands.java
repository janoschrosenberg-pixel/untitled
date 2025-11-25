package main;

public interface EditorCommands {
    void appendChar(char sign);
    void delChar();
    void enter();

    void ctrlPressed();
    void ctrlReleased();
    String showModus();

    void esc();

}
