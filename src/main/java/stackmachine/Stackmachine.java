package stackmachine;



import editor.EditorActions;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;
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
    public Stackmachine(EditorActions editorActions) throws IOException {
        this.editorActions = editorActions;
        try {
            StackUtils.readLines(KEYBINDING_PATH, str -> keyBindings.add(KeyBinding.parse(str)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        BuildInFunctions.addToMap(runnableMap, editorActions, this.stack);




       Map<String, List<String>> loadedCommands = StackUtils.parseFileByColonSections(Paths.get("stacks/ini"));

        for(String key: loadedCommands.keySet()) {
            List<StackCommand> commands = loadedCommands.get(key).stream().map(this::convert).toList();
            Runnable runnable = () -> commands.forEach(this::handleCommand);
            var newKey = key.substring(1);
            runnableMap.put(newKey, runnable);
            wordMap.put(newKey, commands);
        }

        for(KeyBinding keyBinding: keyBindings) {
            this.editorActions.bind(keyBinding.key(),keyBinding.mode(), runnableMap.get(keyBinding.method()));
        }

    }

    @Override
    public void executeCommand(String command) {
        var commands = StackUtils.tokenize(command).toArray(new String[0]);

        if(commands.length>2 && commands[0].equals(":")) {
            var compiledCommands = compileCommands( removeFirstTwo(commands));
            wordMap.put(commands[1], compiledCommands);

            runnableMap.put(commands[1], ()->  handleCommands(compiledCommands));
        }else{
            handleCommands(compileCommands(commands));
        }
    }

    @Override
    public void push(Object o) {
        this.stack.push(o);
    }

    @Override
    public void runCommand(String command) {
        this.runnableMap.get(command).run();
    }

    private void handleCommands(List<StackCommand> stackCommands) {
        for(StackCommand stackCommand: stackCommands) {
            handleCommand(stackCommand);
        }
    }

    private StackCommand convert(String input) {
        String[] parts = input.trim().split("\\s+");
        var opcode = Opcode.fromString(parts[0]);

        String value = null;

        if(parts.length == 2) {
            value = parts[1];
        }

        return new StackCommand(opcode, value);
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
                var mode = binding.mode();
                if(runnableMap.containsKey(method)) {
                    keyBindings.add(binding);
                    editorActions.bind(key, mode, runnableMap.get(method));
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

            case DUP -> {
                stack.push(stack.peek());
            }

            case EQ -> {
                Object v1 = stack.pop();
                Object v2 = stack.pop();

                if(v1.equals(v2)) {
                    stack.push(BigDecimal.ONE);
                }else{
                    stack.push(BigDecimal.ZERO);
                }
            }

            case CALL_IF -> {
                var key = stackCommand.value().toString();
                Object value = stack.pop();

                if (!BigDecimal.ZERO.equals(value)) {
                    if(runnableMap.containsKey(key)){
                        runnableMap.get(key).run();
                    }
                }
            }

            case CALL_IF_NOT -> {
                var key = stackCommand.value().toString();
                Object value = stack.pop();

                if (BigDecimal.ZERO.equals(value)) {
                    if(runnableMap.containsKey(key)){
                        runnableMap.get(key).run();
                    }
                }
            }

            case EXIT -> {
                this.editorActions.exit();
            }

            case LOAD_FILE -> {
                String filename =   this.stack.pop().toString();
                this.editorActions.loadFile(filename);
            }

            case SAVE_BUFFER -> {
                String filename = null;
                if(!this.stack.isEmpty()){
                    filename = this.stack.pop().toString();
                }
                this.editorActions.saveBuffer(filename);
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

        if(isValidTripleText(command)) {
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
                }
                sb.append("\n");
            }

        }
        return sb.toString();
    }

    public List<Object> getStackObjects() {
        List<Object> objects = new ArrayList<>();

        while(!stack.isEmpty()){
            objects.add(stack.pop());
        }

        return objects;
    }
}
