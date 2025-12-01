package main;

public interface EditorActions {
    void loadFile(String file);
    void switchToCommandMode();
    void switchToEditorMode();
    void switchToCustomMode();

    void clearBuffer();

    void setStatus(String status);

    void moveCursorUp(int amount);
    void moveCurserDown(int amount);
    void moveCurserLeft(int amount);
    void moveCurserRight(int amount);

    void scrollUp(int amount);
    void scrollDown(int amount);

    void bind(String key,String mode, Runnable command);

    void returnCommandContext();
    void setEditorMode(String mode);

    void saveBuffer(String file);
    void fullScreenMode();
}
