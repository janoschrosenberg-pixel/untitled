package blocks;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class BlockUtils {
    public static List<Block> findBlocks(String src) {
        List<Block> blocks = new ArrayList<>();
        Deque<Block> stack = new ArrayDeque<>();

        State state = State.CODE;

        int line = 0;
        int lineStartIndex = 0;

        for (int i = 0; i < src.length(); i++) {
            char c = src.charAt(i);
            char next = (i + 1 < src.length()) ? src.charAt(i + 1) : '\0';

            // neue Zeile
            if (c == '\n') {
                line++;
                lineStartIndex = i + 1;
            }

            int column = i - lineStartIndex;

            switch (state) {

                case CODE -> {
                    if (c == '"') state = State.STRING_DOUBLE;
                    else if (c == '\'') state = State.STRING_SINGLE;
                    else if (c == '`') state = State.TEMPLATE;

                    else if (c == '/' && next == '/') {
                        state = State.LINE_COMMENT;
                        i++;
                    }
                    else if (c == '/' && next == '*') {
                        state = State.BLOCK_COMMENT;
                        i++;
                    }

                    else if (c == '{' || c == '(') {
                        stack.push(new Block(
                                c,
                                column,
                                -1,
                                line,
                                -1
                        ));
                    }

                    else if (c == '}' || c == ')') {
                        if (!stack.isEmpty()) {
                            Block open = stack.pop();
                            blocks.add(new Block(
                                    open.open(),
                                    open.startColumn(),
                                    column,
                                    open.startLine(),
                                    line
                            ));
                        }
                    }
                }

                case STRING_SINGLE -> {
                    if (c == '\\') i++;
                    else if (c == '\'') state = State.CODE;
                }

                case STRING_DOUBLE -> {
                    if (c == '\\') i++;
                    else if (c == '"') state = State.CODE;
                }

                case TEMPLATE -> {
                    if (c == '\\') i++;
                    else if (c == '`') state = State.CODE;
                }

                case LINE_COMMENT -> {
                    if (c == '\n') state = State.CODE;
                }

                case BLOCK_COMMENT -> {
                    if (c == '*' && next == '/') {
                        state = State.CODE;
                        i++;
                    }
                }
            }
        }

        return blocks;
    }


}
