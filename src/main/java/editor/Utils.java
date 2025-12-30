package editor;

import parser.MethodScannerUtil;
import stackmachine.StackUtils;
import tokenizer.TokenUtils;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public class Utils {
    private static final String OS =
            System.getProperty("os.name").toLowerCase();
    public static boolean isWindows() {
        return OS.contains("win");
    }

    public static boolean isLinux() {
        return OS.contains("linux");
    }

    public static String getOsConfig() {
        if(isWindows()) {
            return "config_win";
        }
        if(isLinux()) {
            return "config_linux";
        }
        return "config_mac";
    }

    public static List<Line> loadResourceFile(String resourceName) throws IOException {
        List<Line> lines = new ArrayList<>();

        StackUtils.readLines(resourceName, str -> lines.add(new Line(str)));

        return lines;
    }

    public static  List<Line> loadResourceFileReact(String resourceName) throws IOException{
        String code = Files.readString(Path.of(resourceName));
        return getReactLinesFromCode(code);
    }

    public static List<Line> getReactLinesFromCode(String code) throws IOException {
        List<tokenizer.Line> lines = TokenUtils.tokenizeIntoLines(code);
        return lines.stream().map(l -> new Line(l.lineText(), l.tokenList())).collect(Collectors.toList());
    }

    public static List<String> readLines(Path file) throws IOException {
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }

        return lines;
    }

    public static String getHexFromString(String str) { StringBuilder hex = new StringBuilder(); for (byte b : str.getBytes(StandardCharsets.UTF_8)) { hex.append(String.format("%02X", b)); } return hex.toString(); }

    public static byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }



    public static String mergeLines(List<Line> lines) {

            int capacity = 0;
            for (Line line : lines) {
                capacity +=line.text.length() + 1; // für '\n'
            }

            StringBuilder out = new StringBuilder(capacity);

            for (Line line : lines) {
                out.append(line.text).append('\n');
            }

            return out.toString().trim();
    }



    public static <T> Result<T> findIntervalWithIndex(
            List<T> list,
            int x,
            ToIntFunction<T> startFn
    ) {
        int low = 0;
        int high = list.size() - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int start = startFn.applyAsInt(list.get(mid));

            if (x < start) {
                high = mid - 1;
            } else if (x > start) {
                low = mid + 1;
            } else {
                // Exakter Treffer
                T obj = list.get(mid);
                return new Result<>(obj, mid);
            }
        }

        // Nach der Suche gilt:
        // high = Index des größten Start < x   (oder -1)
        // low  = Index des kleinsten Start > x (oder list.size())

        T leftObj  = (high >= 0) ? list.get(high) : null;
        T rightObj = (low < list.size()) ? list.get(low) : null;

        if (leftObj == null && rightObj == null) {
            return null; // leere Liste
        }
        if (leftObj == null) {
            return new Result<>(rightObj, low);
        }
        if (rightObj == null) {
            return new Result<>(leftObj, high);
        }

        int distLeft  = Math.abs(x - startFn.applyAsInt(leftObj));
        int distRight = Math.abs(x - startFn.applyAsInt(rightObj));

        return (distLeft <= distRight)
                ? new Result<>(leftObj, high)
                : new Result<>(rightObj, low);
    }


    public static Result<MethodScannerUtil.MethodInfo> findRangeResult(List<MethodScannerUtil.MethodInfo> ranges, int x) {
        return findIntervalWithIndex(ranges, x, MethodScannerUtil.MethodInfo::startLine);
    }

    public static Color getComplementaryColorHSB(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("Color darf nicht null sein");
        }

        // RGB in HSB umwandeln
        float[] hsbVals = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);

        // Farbton um 180° drehen (0.5 im Bereich 0-1)
        float newHue = (hsbVals[0] + 0.5f) % 1.0f;

        // Neue Farbe aus HSB-Werten erstellen
        return Color.getHSBColor(newHue, hsbVals[1], hsbVals[2]);
    }

    public static int stringWidth(String text, Font font) {
        FontRenderContext frc =
                new FontRenderContext(new AffineTransform(), true, true);
        return (int) font.getStringBounds(text, frc).getWidth();
    }
}
