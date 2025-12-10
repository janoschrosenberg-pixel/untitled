package tokenizer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;
import editor.ColorUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static java.util.regex.Pattern.compile;


public enum Tokenizer {
    ROUND_BRACKET(new Color(255, 68, 68)),
    MODIFIER(new Color(255, 0, 0)),
    STATIC(new Color(255, 0, 255)),
    KEYWORD(new Color(255, 255, 0)),
    IDENTIFIER(new Color(0, 220, 60)),
    NUMBER(new Color(200, 0, 255)),
    BRACKET(new Color(0, 255, 255)),
    STRING(new Color(0, 180, 255)),
    CHAR(new Color(68, 85, 255)),
    COMMENT(new Color(140, 0, 255)),
    OPERATOR(
            new Color(0, 255, 150)
    ),
    WHITESPACE(new Color(255, 0, 150)),
    SYMBOL(new Color(255, 120, 180)),
    UNKNOWN(new Color(255, 180, 50)),
    COMPARE(new Color(80, 255, 200)),
    LOGICAL(new Color(120, 255, 120)),
    COLON(new Color(255, 160, 0)),
    ARITHMETIC(new Color(180, 120, 255));

    private Color textColor;
    private Color backgroundColor;

    Tokenizer(Color textColor) {
        this.textColor = textColor;
        this.backgroundColor = ColorUtils.getHarmonicContrastColor(textColor);
    }
    static JavaParser parser = new JavaParser(
            new ParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21)
    );
    public static List<Token> tokenize(String code) {
        List<Token> tokens = new ArrayList<>();

        ParseResult<CompilationUnit> result = parser.parse(code);

        if (result.getResult().isPresent()) {
            CompilationUnit cu = result.getResult().get();
            TokenRange tokenRange = cu.getTokenRange().get();

            tokenRange.forEach(token -> {
                if(token.getKind() != 0){

                    var newToken = Token.from(token);

                    if(!tokens.isEmpty() && tokens.getLast().type() == newToken.type() &&  tokens.getLast().type() == Tokenizer.WHITESPACE) {
                        var lastStart = tokens.getLast().start();
                        var newEnd = newToken.end();
                        var newType = newToken.type();
                        tokens.removeLast();

                        tokens.add(new Token(newType, lastStart, newEnd));

                    }else{
                        tokens.add(newToken);
                    }
                }

            });
        }

        return tokens;
    }

    public Color getTextColor() {
        return this.textColor;
    }

    public Color getBackgroundColor(){
        return this.backgroundColor;
    }


}
