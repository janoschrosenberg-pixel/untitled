package tokenizer;

import com.github.javaparser.JavaToken;

public record Token(Tokenizer type, int start, int end) {
    public boolean isBetween(int column) {
        return start() <= column && end() > column;
    }

    public static Token from (JavaToken javaToken){
        Tokenizer type = switch (javaToken.getKind()) {

            case 103, 104 -> Tokenizer.BRACKET;
            case 94 -> Tokenizer.STRING;
            case 54 -> Tokenizer.STATIC;
            case 1 -> Tokenizer.WHITESPACE;
            case 98 -> Tokenizer.IDENTIFIER;
            case 81 -> Tokenizer.NUMBER;
            case 38, 19, 36, 65, 42  -> Tokenizer.KEYWORD;
            case 120 -> Tokenizer.OPERATOR;
            case 107, 109 -> Tokenizer.SYMBOL;
            default -> Tokenizer.UNKNOWN;
        };

        var range = javaToken.getRange().orElse(null);
        assert range != null;
        return new Token(type, range.begin.column-1, range.end.column);
    }

}
