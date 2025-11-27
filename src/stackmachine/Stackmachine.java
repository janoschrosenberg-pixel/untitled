package stackmachine;

import main.EditorActions;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static stackmachine.StackUtils.*;

public class Stackmachine implements Inter{
    private static final String KEYBINDING_PATH="keys/keybinding";
    private final Stack<Object> stack = new Stack<>();
    private final Map<String, Object> memory = new HashMap<>();
    private final Set<KeyBinding> keyBindings = new HashSet<>();

    private final Map<String, List<StackCommand>> wordMap = new HashMap<>();


    private final Map<String, Runnable> runnableMap = new HashMap<>();


    private final EditorActions editorActions;
    public Stackmachine(EditorActions editorActions) {
        this.editorActions = editorActions;
        try {
            StackUtils.readLines(KEYBINDING_PATH, str -> keyBindings.add(KeyBinding.parse(str)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // System functions
        runnableMap.put("COMMAND_MODE", this.editorActions::switchToCommandMode);
        runnableMap.put("CURSOR_UP", ()->this.editorActions.moveCursorUp(1));
        runnableMap.put("CURSOR_DOWN", ()->this.editorActions.moveCurserDown(1));
        runnableMap.put("CURSOR_LEFT", ()->this.editorActions.moveCurserLeft(1));
        runnableMap.put("CURSOR_RIGHT", ()->this.editorActions.moveCurserRight(1));
        runnableMap.put("LOAD_FILE", ()->{
            String filename =   this.stack.pop().toString();
            this.editorActions.loadFile(filename);
        } );

        runnableMap.put("SAVE_BUFFER", ()-> {
            String filename = null;
            if(!this.stack.isEmpty()){
                filename = this.stack.pop().toString();
            }
            this.editorActions.saveBuffer(filename);
        });

        runnableMap.put("EXIT", ()->{
            System.exit(0);
        } );

        runnableMap.put("FULLSCREEN", this.editorActions::fullScreenMode);


        for(KeyBinding keyBinding: keyBindings) {
            this.editorActions.bind(keyBinding.key(), runnableMap.get(keyBinding.method()));
        }
    }

    @Override
    public void executeCommand(String command) {
        String[] commands = splitByWhitespace(command);

        if(commands.length>2 && commands[0].equals(":")) {
            var compiledCommands = compileCommands( removeFirstTwo(commands));
            wordMap.put(commands[1], compiledCommands);

            runnableMap.put(commands[1], ()->  handleCommands(compiledCommands));
        }else{
            handleCommands(compileCommands(commands));
        }
    }

    private void handleCommands(List<StackCommand> stackCommands) {
        for(StackCommand stackCommand: stackCommands) {
            handleCommand(stackCommand);
        }
    }

    private void handleCommand(StackCommand stackCommand) {
        switch (stackCommand.opcode()) {
            case PUSH_CHAR, PUSH_DOUBLE, PUSH_STRING -> stack.push(stackCommand.value());
            case PRINT -> {
                var val = stack.pop();
                editorActions.setStatus(val.toString());
            }
            case BIND -> {
                KeyBinding binding = (KeyBinding) stackCommand.value();
                var method = binding.method();
                var key = binding.key();
                if(runnableMap.containsKey(method)) {
                    keyBindings.add(binding);
                    editorActions.bind(key, runnableMap.get(method));
                    try {
                        StackUtils.writeLinesToFile(keyBindings.stream()
                                .map(KeyBinding::toString)
                                .collect(Collectors.toSet()), KEYBINDING_PATH);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            case WRITE_CODE -> {
                var fileName = stack.pop();
                writeFile(fileName.toString());
            }
            case CALL -> {
                var key = stackCommand.value().toString();
                if(runnableMap.containsKey(key)){
                    runnableMap.get(key).run();
                }
            }
        }
    }


    private List<StackCommand> compileCommands(String[] commands) {
        List<StackCommand> stackCommands = new ArrayList<>();
        for(String currentCommand: commands) {
            stackCommands.add(compileCommand(currentCommand));
        }


        return stackCommands;
    }

    private StackCommand compileCommand(String command) {
        if(isDouble(command)) {
             return new StackCommand(Opcode.PUSH_DOUBLE, new BigDecimal(command));
        }
        if(isChar(command)){
            return new StackCommand(Opcode.PUSH_CHAR, command.charAt(1));
        }
        if(isString(command)){
            return new StackCommand(Opcode.PUSH_STRING, removeFirstAndLast(command));
        }
        if(command.length() == 1 && command.charAt(0) == '+') {
            return new StackCommand(Opcode.ADD, null);
        }

        if(isCharStringFormat(command)) {
            return new StackCommand(Opcode.BIND, KeyBinding.parse(command));
        }

        if(Opcode.matches(command)) {
            return new StackCommand(Opcode.fromString(command), null);
        }
        
        return new StackCommand(Opcode.CALL, command);
    }

    private void writeFile(String fileName) {
        try {
            StackUtils.writeFile("stacks", fileName, wordMap2String());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String wordMap2String() {
        StringBuilder sb = new StringBuilder();

        for(String key:wordMap.keySet()) {
            sb.append(":");
            sb.append(key);
            sb.append("\n");

            var list=wordMap.get(key);

            for(StackCommand sc:list) {
                sb.append(sc.opcode().name());
                if(sc.value() != null) {
                    sb.append(" ");
                    sb.append(sc.value());
                    sb.append("\n");
                }
            }

        }
        return sb.toString();
    }
}
