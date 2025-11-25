package main;

import java.util.HashMap;

public class CustomCommandMode implements EditorCommands{

    private final HashMap<Character, Runnable> bindingMap = new HashMap<>();

    private final EditorActions editorActions;
    CustomCommandMode(EditorActions editorActions){
        this.editorActions = editorActions;
    }
    public void bind(char sign, Runnable command) {
        bindingMap.put(sign, command);
    }

    @Override
    public void appendChar(char sign) {
        var runnable = bindingMap.get(sign);
        if(runnable != null) {
            runnable.run();
        }
    }

    @Override
    public void delChar() {

    }

    @Override
    public void enter() {

    }

    @Override
    public void ctrlPressed() {
        editorActions.switchToEditorMode();
    }

    @Override
    public void ctrlReleased() {

    }

    @Override
    public String showModus() {
        return "Custom Mode";
    }

    @Override
    public void esc() {

    }
}
