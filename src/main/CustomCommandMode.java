package main;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class CustomCommandMode implements EditorCommands{

    private final HashMap<String, Map<String, Runnable>> bindingMap = new HashMap<>();
    private final Stack<String> mode = new Stack<>();

    CustomCommandMode(){
        mode.push("normal");
    }
    public void bind(String key, String mode, Runnable command) {
        if(!bindingMap.containsKey(mode)) {
            bindingMap.put(mode, new HashMap<>());
        }
        bindingMap.get(mode).put(key, command);
    }

    public void returnContext() {
        if(mode.size()>1) {
            mode.pop();
        }

    }

    public void setMode(String mode) {
        this.mode.push(mode);
    }

    @Override
    public void appendChar(char sign) {
        typeCommand(sign+"");
    }

    private void typeCommand(String sign) {
        var runnable = bindingMap.get(mode.peek()).get( sign);
        if(runnable != null) {
            runnable.run();
        }
    }

    @Override
    public void delChar() {
        typeCommand("del");
    }

    @Override
    public void enter() {
        typeCommand("enter");
    }

    @Override
    public void ctrlPressed() {
        typeCommand("ctrl");
    }

    @Override
    public void ctrlReleased() {

    }

    @Override
    public String showModus() {
        return "Custom Mode/"+mode.peek();
    }

    @Override
    public void esc() {
        typeCommand("esc");
    }
}
