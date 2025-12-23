package stackmachine;

import java.util.List;

public interface Inter {
     void executeCommand(String command);
     void push(Object o);
     void runCommand(String command);

      List<Object> getStackObjects();
}