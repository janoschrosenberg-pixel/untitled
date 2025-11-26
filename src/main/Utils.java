package main;

import stackmachine.StackUtils;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static List<StringBuilder> loadResourceFile(String resourceName) throws IOException {
        List<StringBuilder> lines = new ArrayList<>();

        StackUtils.readLines(resourceName, str -> lines.add(new StringBuilder(str)));

        return lines;
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

    public static void giveFocusTo(JComponent comp) {
        SwingUtilities.invokeLater(() -> {
            comp.requestFocusInWindow();
            if (comp instanceof JTextField tf) tf.selectAll();
            if (comp instanceof JPasswordField pf) pf.selectAll();
        });
    }
}
