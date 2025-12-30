package tokenizer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import tokenizer.json.JsonToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class TokenUtils {
    public static boolean isOnlyWhitespace(String s) {
        return s != null && s.chars().allMatch(Character::isWhitespace);
    }

    private static List<Integer> computeLineStarts(String source) {
        List<Integer> starts = new ArrayList<>();
        starts.add(0);

        for (int i = 0; i < source.length(); i++) {
            if (source.charAt(i) == '\n') {
                starts.add(i + 1);
            }
        }

        return starts;
    }

    public static List<Token>  tokenize(String code) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("node", "tokenizer.js");
        Process process = pb.start();

        // Code an Node-Process schreiben
        try (OutputStream os = process.getOutputStream()) {
            os.write(code.getBytes());
            os.flush();
        }

        // Tokens lesen
        try (InputStream is = process.getInputStream()) {
            String tokensJson = new String(is.readAllBytes());

            ObjectMapper mapper = new ObjectMapper();
            List<JsonToken> tokens = mapper.readValue(
                    tokensJson,
                    new TypeReference<>() {
                    }
            );

            return mapWithUnknowns(tokens, code.length());
        }
    }

    public static List<Token> mapWithUnknowns(List<JsonToken> tokens, int sourceLength) {
        List<Token> result = new ArrayList<>();

        // Sicherheit: sortieren nach Startposition
        tokens.sort(Comparator.comparingInt(JsonToken::start));

        int cursor = 0;

        for (JsonToken t : tokens) {

            if(t.type().label().equals("eof")) {
                continue;
            }
            // 🔹 Lücke vor dem Token?
            if (t.start() > cursor) {
                result.add(new Token(
                        Tokenizer.UNKNOWN,
                        cursor,
                        t.start()
                ));
            }

            // 🔹 Eigentliches Token
            result.add(new Token(
                    TokenMapper.map(t),
                    t.start(),
                    t.end()
            ));

            cursor = t.end();
        }

        // 🔹 Lücke am Ende?
        if (cursor < sourceLength) {
            result.add(new Token(
                    Tokenizer.UNKNOWN,
                    cursor,
                    sourceLength
            ));
        }

        return result;
    }
    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    public static List<Line> tokenizeIntoLines(String source) throws IOException {
        List<Token> tokens;
        try {
            tokens = tokenize(source);
        }catch (Exception e){
            String[] code = source.split("\n");
         return Arrays.stream(code).map(line -> new Line(new ArrayList<>(), line)).toList();
        }
        List<Integer> lineStarts = computeLineStarts(source);

        // Lines erzeugen
        List<Line> lines = new ArrayList<>();
        for (int i = 0; i < lineStarts.size(); i++) {
            int start = lineStarts.get(i);
            int end = (i + 1 < lineStarts.size())
                    ? lineStarts.get(i + 1)
                    : source.length();

            String text = source.substring(start, end);
            if (text.endsWith("\n")) {
                text = text.substring(0, text.length() - 1);
            }

            lines.add(new Line(new ArrayList<>(), text));
        }

        // Tokens verteilen
        for (Token token : tokens) {
            int start = token.start();
            int end = token.end();

            int startLine = findLine(start, lineStarts);
            int endLine = findLine(end, lineStarts);

            for (int line = startLine; line <= endLine; line++) {
                int lineStart = lineStarts.get(line);
                int lineEnd = (line + 1 < lineStarts.size())
                        ? lineStarts.get(line + 1)
                        : source.length();

                // Bereich relativ zur Zeile
                int s = Math.max(start, lineStart) - lineStart;
                int e = Math.min(end, lineEnd) - lineStart;

                // Clamp gegen Zeilenlänge
                int lineLength = lines.get(line).lineText().length();
                s = clamp(s, 0, lineLength);
                e = clamp(e, 0, lineLength);

                if (s < e) {
                    lines.get(line).tokenList().add(
                            new Token(token.type(), s, e)
                    );
                }
            }
        }

        return lines;
    }


    private static int findLine(int index, List<Integer> lineStarts) {
        int low = 0;
        int high = lineStarts.size() - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int start = lineStarts.get(mid);

            if (start <= index) {
                if (mid == lineStarts.size() - 1 || lineStarts.get(mid + 1) > index) {
                    return mid;
                }
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        return 0;
    }

}
