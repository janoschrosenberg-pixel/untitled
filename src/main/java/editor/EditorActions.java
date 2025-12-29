package editor;

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

    void toNextWord();
    void toPrevWord();

    void toNextMethod();
    void toPrevMethod();

    void openMenu(String name);
    void sendMenuCommand(String command);
    void closeMenu();

    void registerMenuFunction(String menuName, String name, String function);
    void registerKeyListener(String mode, String function);
    void registerWorkspace(String path);

    void startLanguageServer();

    void findDefinition();


    void exit();

    void javaFiles2Stack();

    void switchTech(String tech);

}
