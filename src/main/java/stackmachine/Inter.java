package stackmachine;

public interface Inter {
     void executeCommand(String command);
     void push(Object o);
     void runCommand(String command);
}
