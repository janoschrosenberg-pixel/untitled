package indexer;

import editor.Tech;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class JavaFileIndex {

    private Map<Tech,  Set<Path>> pathes = new HashMap<>();


    void add(Tech t, Path p)    {
        if(!pathes.containsKey(t)) {
            pathes.put(t,  ConcurrentHashMap.newKeySet());
        }
        pathes.get(t).add(p);
    }

    public Set<Path> snapshot(Tech t) {
        return Set.copyOf( pathes.get(t));
    }
}
