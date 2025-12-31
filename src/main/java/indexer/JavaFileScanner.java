package indexer;

import editor.Tech;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class JavaFileScanner {

   public static void scan(Tech tech, Path root, JavaFileIndex index,  Predicate<Path> pathPredicate) throws IOException {
        boolean parallel = Runtime.getRuntime().availableProcessors() > 4;

        try (Stream<Path> stream = Files.walk(root)) {
            Stream<Path> s = parallel ? stream.parallel() : stream;

          //  Predicate<Path> pathPredicate = p -> p.getFileName().toString().endsWith(".java");
            s.filter(pathPredicate)
                    .filter(p -> !isIgnored(p))
                    .forEach(path -> index.add(tech, path));
        }
    }

    public static void scan(Tech tech, Path root, JavaFileIndex index) throws IOException{
       switch(tech) {
           case JAVA -> scan(Tech.JAVA,root, index, p -> p.getFileName().toString().endsWith(".java"));
           case REACT -> scan(Tech.REACT, root, index, p ->
                   p.getFileName().toString().endsWith(".tsx") ||
                   p.getFileName().toString().endsWith(".ts") ||
                   p.getFileName().toString().endsWith(".jsx") ||
                   p.getFileName().toString().endsWith(".js") ||
                   p.getFileName().toString().endsWith(".html") ||
                   p.getFileName().toString().endsWith(".css"));
       }
    }

    static List<String> ignoreList = List.of("target", "build", "out", "node_modules", ".gradle");
    static boolean isIgnored(Path p) {
        String s = p.toString();

        for(String part: ignoreList) {
            if(ignoreString(s, part)) {
                return true;
            }
        }

        return false;
    }

    private static boolean ignoreString(String complete, String t) {
       return complete.contains("\\"+t+"\\") || complete.contains("/"+t+"/");
    }
}
