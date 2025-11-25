package stackmachine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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

    public static boolean isCharStringFormat(String s) {
        if (s == null) return false;

        return s.matches("^[A-Za-z0-9]:.+$");
    }

    public static void writeLinesToFile(Set<String> lines, String filePath) throws IOException {
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



}
