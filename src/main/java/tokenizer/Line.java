package tokenizer;

import java.util.List;

public record Line(List<Token> tokenList, String lineText) {
}
