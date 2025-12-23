package indexer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class JavaFileScanner {

   public static void scan(Path root, JavaFileIndex index) throws IOException {
        boolean parallel = Runtime.getRuntime().availableProcessors() > 4;

        try (Stream<Path> stream = Files.walk(root)) {
            Stream<Path> s = parallel ? stream.parallel() : stream;

            s.filter(p -> p.getFileName().toString().endsWith(".java"))
                    .filter(p -> !isIgnored(p))
                    .forEach(index::add);
        }
    }

    static boolean isIgnored(Path p) {
        String s = p.toString();
        return s.contains("/target/")
            || s.contains("/build/")
            || s.contains("/out/")
            || s.contains("/.gradle/");
    }
}
