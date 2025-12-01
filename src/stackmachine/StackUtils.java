package stackmachine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class StackUtils {
    public static boolean isDouble(String str) {
        return str != null && str.strip().matches("-?\\d*\\.?\\d+([eE][-+]?\\d+)?");
    }

    public static boolean isString(String str) {
        return str != null && str.startsWith("\"") && str.endsWith("\"");
    }

    public static boolean isChar(String str) {
        if(str == null) {
            return false;
        }
        if(str.length() != 3){
            return false;
        }

        return str.charAt(0) == '\'' && str.charAt(2)== '\'';
    }

    public static String[] splitByWhitespace(String input) {
        if (input == null || input.isEmpty()) {
            return new String[0];
        }
        return input.trim().split("\\s+");
    }

    public static String removeFirstAndLast(String s) {
        if (s == null || s.length() < 2) {
            return "";
        }
        return s.substring(1, s.length() - 1);
    }
    public static String joinAfterTwo(String[] arr) {
        if (arr == null || arr.length <= 2) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 2; i < arr.length; i++) {
            if (i > 2) sb.append(' ');
            sb.append(arr[i]);
        }

        return sb.toString();
    }

    public static String[] removeFirstTwo(String[] arr) {
        if (arr == null || arr.length <= 2) {
            return new String[0];
        }

        String[] result = new String[arr.length - 2];
        System.arraycopy(arr, 2, result, 0, arr.length - 2);
        return result;
    }

    public static Path writeFile(String directoryPath, String fileName, String content) throws IOException {

        Path dir = Paths.get(directoryPath);

        // Sicherstellen, dass das Verzeichnis existiert
        Files.createDirectories(dir);

        Path filePath = dir.resolve(fileName);

        Files.write(
                filePath,
                content.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );

        return filePath;
    }

    public static void readLines(String filePath, Consumer<String> lineHandler) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(Path.of(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                lineHandler.accept(line);
            }
        }
    }

    public static boolean isValidTripleText(String input) {
        if (input == null) return false;
        return input.matches("^[^:]+:[^:]+:[^:]+$");
    }

    public static void writeLinesToFile(Collection<String> lines, String filePath) throws IOException {
        Path path = Path.of(filePath);

        try (BufferedWriter writer = Files.newBufferedWriter(
                path,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        )) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    public static List<String> tokenize(String code) {
        List<String> tokens = new ArrayList<>();
        int i = 0;
        int n = code.length();

        while (i < n) {

            // Whitespace überspringen
            if (Character.isWhitespace(code.charAt(i))) {
                i++;
                continue;
            }

            // -------- STRING LITERAL --------
            if (code.charAt(i) == '"') {
                int start = i;
                i++; // erstes "
                while (i < n && code.charAt(i) != '"') {
                    i++;
                }
                if (i < n) i++; // abschließendes "
                tokens.add(code.substring(start, i));
                continue;
            }

            // -------- START ... END BLOCK --------
            if (code.startsWith("START", i)) {
                int start = i;
                i += 5; // Länge von "START"

                // vorwärts suchen bis END
                while (i < n && !code.startsWith("END", i)) {
                    i++;
                }

                if (i < n) {
                    i += 3; // Länge von "END"
                }

                tokens.add(code.substring(start, i));
                continue;
            }

            // -------- NORMALER TOKEN --------
            int start = i;
            while (i < n && !Character.isWhitespace(code.charAt(i))) {
                // Wenn wir auf Strings oder START/END treffen -> abbrechen
                if (code.charAt(i) == '"') break;
                if (code.startsWith("START", i)) break;
                if (code.startsWith("END", i)) break;

                i++;
            }
            tokens.add(code.substring(start, i));
        }

        return tokens;
    }


}
