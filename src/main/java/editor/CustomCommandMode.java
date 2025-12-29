package editor;

import stackmachine.Inter;

import java.util.*;

public class CustomCommandMode implements EditorCommands{

    private final HashMap<String, Map<String, Runnable>> bindingMap = new HashMap<>();
    private final Stack<String> mode = new Stack<>();

    private final TempBuffer tempBuffer = TempBuffer.INSTANCE;

    private Map<String, List<String>> listener = new HashMap<>();

    private Inter inter;
    CustomCommandMode(){
        mode.push("normal");

    }

    public void setStackMachine(Inter inter){
        this.inter = inter;
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

        String mode = this.mode.peek();
        if(mode.startsWith("insert")) {
            this.tempBuffer.add(sign);
            handleListeners(mode);
            return;
        }

        if(sign == ' ') {
            typeCommand("space");
        }else{
            typeCommand(sign+"");
        }


    }

    private void handleListeners(String mode) {
        if(listener.containsKey(mode)) {
            List<String> commands = listener.get(mode);
            commands.forEach( this.inter::runCommand);
        }
    }

    public void registerListener(String mode, String command) {
        if(!this.listener.containsKey(mode)) {
            this.listener.put(mode, new ArrayList<>());
        }

        this.listener.get(mode).add(command);
    }

    private void typeCommand(String sign) {
        var runnable = bindingMap.get(mode.peek()).get( sign);
        if(runnable != null) {
            runnable.run();
        }
    }

    @Override
    public void delChar() {
        String mode = this.mode.peek();
        if(mode.startsWith("insert")) {
            this.tempBuffer.del();
            handleListeners(mode);
            return;
        }
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
