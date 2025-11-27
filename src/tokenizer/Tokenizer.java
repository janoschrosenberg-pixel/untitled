package tokenizer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Comparator.comparingInt;
import static java.util.regex.Pattern.MULTILINE;
import static java.util.regex.Pattern.compile;

public enum Tokenizer {
    //KEYWORD(compile("\\b(int|String|double|var|float|class|public|static|void|private|import|throws|main|new|true|false)\\b")),
    NUMBER(compile("\\b[0-9]+\\b")),
    IDENTIFIER(compile("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b")),
    CUSTOM(null);

    private Pattern pattern;

    Tokenizer(Pattern pattern) {
        this.pattern = pattern;
    }

    public static List<Token> tokenize(String code) {
        List<Token> tokens = new ArrayList<>();

        for (var entry : values()) {
            if(CUSTOM == entry) {
                continue;
            }
            Pattern pattern = entry.pattern;

            Matcher matcher = pattern.matcher(code);
            while (matcher.find()) {
                tokens.add(new Token(entry, matcher.start(), matcher.end()));
            }
        }

        tokens.sort(comparingInt(Token::start));


        List<Token> result = new ArrayList<>();
        int pos = 0;

        for (Token t : tokens) {
            if (t.start() > pos) {

                result.add(new Token(CUSTOM, pos, t.start()));
            }
            result.add(t);
            pos = t.end();
        }

        // after last token to end-of-string
        if (pos < code.length()) {
            result.add(new Token(CUSTOM, pos, code.length()));
        }
        return result;
    }


}
