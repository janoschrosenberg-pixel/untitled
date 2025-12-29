package tokenizer;

import com.github.javaparser.*;
import com.github.javaparser.ParserConfiguration.LanguageLevel;
import com.github.javaparser.printer.lexicalpreservation.*;
import com.github.javaparser.Providers;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.Comment;

public class JavaParserTokenizeExample {

    public static void main(String[] args) {
        String code = "int   xatan =  33;";


        JavaParser parser = new JavaParser(
                new ParserConfiguration().setLanguageLevel(LanguageLevel.JAVA_21)
        );

        // Code wird geparst → liefert AST + TokenRange
        ParseResult<CompilationUnit> result = parser.parse(code);

        if (result.getResult().isPresent()) {
            CompilationUnit cu = result.getResult().get();

            TokenRange tokenRange = cu.getTokenRange().get();

            tokenRange.forEach(token -> {
                System.out.println(Token.from(token));
            });
        }
    }
}
