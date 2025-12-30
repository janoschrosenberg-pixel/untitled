package tokenizer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import editor.Utils;
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

        TokenUtils.tokenizeIntoLines(code);

        Utils.loadResourceFileReact("/home/oem/vite/packages/create-vite/template-react-ts/src/App.tsx");
    }


}
