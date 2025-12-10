package parser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class MethodScannerUtil {

    private static final JavaParser PARSER;

    static {
        ParserConfiguration cfg = new ParserConfiguration();
        cfg.setAttributeComments(false);  // schneller
        cfg.setLexicalPreservationEnabled(false);
        PARSER = new JavaParser(cfg);
    }

    private MethodScannerUtil() {}

    /**
     * Repräsentiert alle relevanten Daten einer Methode für Syntax-Highlighting oder Folding.
     */
    public record MethodInfo(
            String name,
            String returnType,
            int startLine,
            int startColumn,
            int endLine,
            int endColumn,
            int bodyStartLine,
            int bodyStartColumn,
            int bodyEndLine,
            int bodyEndColumn
    ) {}

    /**
     * Scannt Java-Code aus einem String.
     */
    public static List<MethodInfo> scan(String code) {
        var result = PARSER.parse(code).getResult();
        if (result.isEmpty()) return List.of();

        return extractMethods(result.get());
    }

    /**
     * Scannt Java-Code aus einer Datei.
     */
    public static List<MethodInfo> scan(Path file) throws IOException {
        String code = Files.readString(file);
        return scan(code);
    }

    /**
     * Extrahiert alle Methoden und deren Positionen.
     */
    private static List<MethodInfo> extractMethods(CompilationUnit cu) {
        List<MethodInfo> list = new ArrayList<>();

        for (MethodDeclaration method : cu.findAll(MethodDeclaration.class)) {

            var r = method.getRange().orElse(null);
            var br = method.getBody().flatMap(b -> b.getRange()).orElse(null);

            if (r == null || br == null) continue;

            list.add(new MethodInfo(
                    method.getNameAsString(),
                    method.getType().asString(),
                    r.begin.line,
                    r.begin.column,
                    r.end.line,
                    r.end.column,
                    br.begin.line,
                    br.begin.column,
                    br.end.line,
                    br.end.column
            ));
        }

        return list;
    }
}