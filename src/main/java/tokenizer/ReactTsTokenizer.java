package tokenizer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import tokenizer.json.JsonToken;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ReactTsTokenizer {
    public static void main(String[] args) throws IOException {
        String code = Files.readString(Path.of("/home/oem/vite/packages/create-vite/template-react-ts/src/App.tsx"));

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

            System.out.println(code.substring(902));

            System.out.println(mapWithUnknowns(tokens, code.length()));
        }
    }

    public static List<Token> mapWithUnknowns(List<JsonToken> tokens, int sourceLength) {
        List<Token> result = new ArrayList<>();

        // Sicherheit: sortieren nach Startposition
        tokens.sort(Comparator.comparingInt(JsonToken::start));

        int cursor = 0;

        for (JsonToken t : tokens) {
            if(t.start() == 902) {
                System.out.println(t);
            }
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
}
