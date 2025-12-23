package indexer;

import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class JavaFileIndex {
    private final Set<Path> javaFiles =
        ConcurrentHashMap.newKeySet();

    void add(Path p)    { javaFiles.add(p); }
    void remove(Path p){ javaFiles.remove(p); }

    public Set<Path> snapshot() {
        return Set.copyOf(javaFiles);
    }
}
