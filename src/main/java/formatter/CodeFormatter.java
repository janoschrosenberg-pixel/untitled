package formatter;

import editor.Line;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CodeFormatter {

    public static void formatCode(List<Line> code) {
        String original = joinLines(code);

        String formatted = tryFormatWithPrettier(original);

        if (formatted == null || formatted.isBlank()) {
            System.err.println("[Formatter] Formatting failed – keeping original code.");
            return;
        }

        // Update existing Line objects
        String[] lines = formatted.split("\n", -1);

        code.clear();
        for (String line : lines) {
            code.add(new Line(line));
        }
    }

    private static String joinLines(List<Line> code) {
        StringBuilder sb = new StringBuilder();
        for (Line line : code) {
            sb.append(line.text).append('\n');
        }
        return sb.toString();
    }

    private static String tryFormatWithPrettier(String source) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "npx", "prettier",
                    "--parser", "typescript"
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (OutputStream os = process.getOutputStream()) {
                os.write(source.getBytes(StandardCharsets.UTF_8));
            }

            String output;
            try (InputStream is = process.getInputStream()) {
                output = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }

            int exit = process.waitFor();
            if (exit != 0 || output.isBlank()) {
                System.err.println("[Formatter] Prettier failed:");
                System.err.println(output);
                return null;
            }

            return output;

        } catch (Exception e) {
            System.err.println("[Formatter] Exception:");
            e.printStackTrace();
            return null;
        }
    }
}
