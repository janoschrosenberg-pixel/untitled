package tokenizer;


import com.github.javaparser.JavaToken;

import static tokenizer.Tokenizer.WHITESPACE;

public record Token(Tokenizer type, int start, int end) {

    public boolean noWhitespace(){
        return type != WHITESPACE;
    }

    public static Token from (JavaToken javaToken){
        Tokenizer type = switch (javaToken.getKind()) {
            case 5 -> Tokenizer.COMMENT;
            case 128, 127, 129,137, 138, 126 -> Tokenizer.ARITHMETIC;
            case 108 -> Tokenizer.COLON;
            case 101, 102 -> Tokenizer.ROUND_BRACKET;
            case 103, 104 -> Tokenizer.BRACKET;
            case 45, 49, 47 -> Tokenizer.MODIFIER;
            case 123, 115, 124 -> Tokenizer.LOGICAL;
            case 114, 150, 113, 121, 122 -> Tokenizer.COMPARE;
            case 94 -> Tokenizer.STRING;
            case 54 -> Tokenizer.STATIC;
            case 1 -> WHITESPACE;
            case 98 -> Tokenizer.IDENTIFIER;
            case 81 -> Tokenizer.NUMBER;
            case 38, 19, 36, 65, 42, 59, 51, 34, 44, 25, 67, 18, 32, 13, 93  -> Tokenizer.KEYWORD;
            case 120 -> Tokenizer.OPERATOR;
            case 107, 109, 119, 112 -> Tokenizer.SYMBOL;
            default -> Tokenizer.UNKNOWN;
        };

        var range = javaToken.getRange().orElse(null);
        assert range != null;
        return new Token(type, range.begin.column-1, range.end.column);
    }



}
