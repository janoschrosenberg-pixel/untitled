package editor;

public interface EditorActions {
    void loadFile(String file);
    void switchToCommandMode();
    void switchToEditorMode();
    void switchToCustomMode();

    void clearBuffer();

    void setStatus(String status);

    void moveCursorUp(int amount);
    void moveCursorDown(int amount);
    void moveCursorLeft(int amount);
    void moveCursorRight(int amount);

    void scrollUp(int amount);
    void
    scrollDown(int amount);

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

    void closeMenu();


    void registerWorkspace(String path);

    void startLanguageServer();

    void findDefinition();

    void updateTokens();
    void exit();

    void javaFiles2Stack();

    void switchTech(String tech);
    void formatCode();

    String getCurrentTech();

    void putSelectionOnStack();

    void selectCurrentLine();
}
