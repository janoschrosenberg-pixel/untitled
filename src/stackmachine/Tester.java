package stackmachine;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Tester {
    public static void main(String[] args) throws IOException {
        StackUtils.readLines("stacks/keybindings", System.out::println);
    }

    public void transformKeybinding(Map<String, List<StackCommand>> map){

    }
}
