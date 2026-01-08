package tokenizer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;
import com.googlecode.lanterna.TextColor;
import editor.MainFrame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public enum Tokenizer {

    ROUND_BRACKET(color(196)),   // bright red
    MODIFIER(color(203)),        // light red
    STATIC(color(201)),          // bright magenta
    KEYWORD(color(226)),         // bright yellow
    IDENTIFIER(color(82)),       // vivid green
    NUMBER(color(141)),          // bright purple
    BRACKET(color(51)),          // bright cyan
    STRING(color(45)),           // light cyan
    CHAR(color(75)),             // light blue
    COMMENT(color(244)),         // neutral gray (absichtlich zurückhaltend)
    OPERATOR(color(48)),         // teal
    WHITESPACE(color(250)),      // near white (für Debug / sichtbar)
    SYMBOL(color(210)),          // soft pink
    UNKNOWN(color(214)),         // orange
    COMPARE(color(39)),          // blue-cyan
    LOGICAL(color(118)),         // bright green
    COLON(color(208)),           // orange
    ARITHMETIC(color(147));      // lavender

    /** Einheitlicher dunkler Hintergrund */
    private static final TextColor BACKGROUND = new TextColor.Indexed(16);

    private final TextColor foreground;


    Tokenizer(TextColor foreground) {
        this.foreground = foreground;
    }

    // ---------- Farb-Helfer ----------

    private static TextColor color(int index) {
        return new TextColor.Indexed(index);
    }

    // ---------------- Tokenizer-Logik unverändert ----------------

    static JavaParser parser = new JavaParser(
            new ParserConfiguration()
                    .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21)
    );

    private static List<Token> tokenizeJava(String code) {
        List<Token> tokens = new ArrayList<>();

        ParseResult<CompilationUnit> result = parser.parse(code);

        if (result.getResult().isPresent()) {
            CompilationUnit cu = result.getResult().get();
            TokenRange tokenRange = cu.getTokenRange().get();

            tokenRange.forEach(token -> {
                if (token.getKind() != 0) {
                    var newToken = Token.from(token);

                    if (!tokens.isEmpty()
                            && tokens.getLast().type() == newToken.type()
                            && tokens.getLast().type() == Tokenizer.WHITESPACE) {

                        var lastStart = tokens.getLast().start();
                        var newEnd = newToken.end();
                        var newType = newToken.type();
                        tokens.removeLast();
                        tokens.add(new Token(newType, lastStart, newEnd));
                    } else {
                        tokens.add(newToken);
                    }
                }
            });
        }
        return tokens;
    }

    public static List<Token> tokenize(String code) {
        return switch (MainFrame.tech) {
            case JAVA -> tokenizeJava(code);
            case REACT -> tokenizeReact(code);
        };
    }

    private static List<Token> tokenizeReact(String code) {
        try {
            return TokenUtils.tokenize(code);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // ---------------- Lanterna-Getter ----------------

    public TextColor getForeground() {
        return foreground;
    }

}
