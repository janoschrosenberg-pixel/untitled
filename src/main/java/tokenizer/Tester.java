package tokenizer;

import stackmachine.StackUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Stack;

public class Tester {
    public static void main(String[] args) throws IOException {
        var f = File.listRoots();
        Arrays.stream(f).sorted().forEach(System.out::println);
    }
}
