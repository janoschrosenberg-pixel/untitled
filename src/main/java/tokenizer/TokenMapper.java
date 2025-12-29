package tokenizer;

import tokenizer.json.JsonToken;

import java.util.Set;

public class TokenMapper {

    // === Gruppen ===

    private static final Set<String> KEYWORDS = Set.of(
            "if", "else", "for", "while", "return", "function", "class",
            "const", "let", "var", "import", "export", "extends",
            "new", "this", "super", "try", "catch", "finally",
            "throw", "switch", "case", "default", "break", "continue",
            "await", "async", "yield", "typeof", "instanceof"
    );

    private static final Set<String> MODIFIERS = Set.of(
            "public", "private", "protected", "static", "readonly"
    );

    private static final Set<String> LOGICAL = Set.of(
            "&&", "||", "!"
    );

    private static final Set<String> COMPARE = Set.of(
            "==", "!=", "===", "!==", "<", "<=", ">", ">="
    );

    private static final Set<String> ARITHMETIC = Set.of(
            "+", "-", "*", "/", "%", "**"
    );

    private static final Set<String> BRACKETS = Set.of(
            "{", "}", "[", "]"
    );

    private static final Set<String> ROUND_BRACKETS = Set.of(
            "(", ")"
    );

    private static final Set<String> SYMBOLS = Set.of(
            ".", ",", ";", "=", "=>"
    );

    // ================================

    public static Tokenizer map(JsonToken token) {
        String label = token.type().label();
        Object value = token.value();

        // --- whitespace (explizit erzeugt)
        if (label.equals("whitespace")) {
            return Tokenizer.WHITESPACE;
        }

        // --- identifiers
        if (label.equals("name") || label.equals("privateName") || label.equals("jsxName")) {
            return Tokenizer.IDENTIFIER;
        }

        // --- literals
        if (label.equals("string")) return Tokenizer.STRING;
        if (label.equals("num")) return Tokenizer.NUMBER;
        if (label.equals("regexp")) return Tokenizer.STRING;

        // --- comments (falls du sie aktivierst)
        if (label.contains("comment")) return Tokenizer.COMMENT;

        // --- JSX
        if (label.startsWith("jsx")) {
            return Tokenizer.IDENTIFIER;
        }

        // --- keywords
        if (KEYWORDS.contains(label)) return Tokenizer.KEYWORD;

        // --- modifiers
        if (MODIFIERS.contains(label)) return Tokenizer.MODIFIER;

        if(BRACKETS.contains(label)) return Tokenizer.BRACKET;
        if(ROUND_BRACKETS.contains(label)) return Tokenizer.ROUND_BRACKET;
        if(SYMBOLS.contains(label)) return Tokenizer.SYMBOL;


        // --- operators (nach value!)
        if (value instanceof String v) {

            if (LOGICAL.contains(v)) return Tokenizer.LOGICAL;
            if (COMPARE.contains(v)) return Tokenizer.COMPARE;
            if (ARITHMETIC.contains(v)) return Tokenizer.ARITHMETIC;

            if (ROUND_BRACKETS.contains(v)) return Tokenizer.ROUND_BRACKET;
            if (BRACKETS.contains(v)) return Tokenizer.BRACKET;
            if (SYMBOLS.contains(v)) return Tokenizer.SYMBOL;
            if (v.equals(":")) return Tokenizer.COLON;
        }

        return Tokenizer.UNKNOWN;
    }
}
