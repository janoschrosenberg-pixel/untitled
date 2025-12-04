package tokenizer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Comparator.comparingInt;
import static java.util.regex.Pattern.MULTILINE;
import static java.util.regex.Pattern.compile;

public enum Tokenizer {
    ROUND_BRACKET,
    MODIFIER,
    BRACKET,
    STATIC,
    KEYWORD,
    IDENTIFIER,
    NUMBER,
    STRING,
    CHAR,
    COMMENT,
    OPERATOR,
    WHITESPACE,
    SYMBOL,
    UNKNOWN, COMPARE, LOGICAL, COLON, ARITHMETIC;
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



}
