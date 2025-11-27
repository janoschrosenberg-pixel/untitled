package tokenizer;

import stackmachine.StackUtils;

import java.io.IOException;
import java.util.Stack;

public class Tester {
    public static void main(String[] args) throws IOException {
        StringBuilder sb = new StringBuilder();
        StackUtils.readLines("test.js", sb::append);

       var tokens= Tokenizer.tokenize(sb.toString());
        for(Token token: tokens) {
            System.out.println(token);
        }
    }
}
