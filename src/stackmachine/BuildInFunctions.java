package stackmachine;

import main.EditorActions;

import java.util.Map;
import java.util.function.Function;

public enum BuildInFunctions {
        COMMAND_MODE(e->e::switchToCommandMode),
        EDITOR_MODE(e->e::switchToEditorMode),
        CURSOR_UP(e->()->e.moveCursorUp(1)),
        CURSOR_DOWN(e->()->e.moveCurserDown(1)),
        CURSOR_LEFT(e->()->e.moveCurserLeft(1)),
        CURSOR_RIGHT(e->()->e.moveCurserRight(1)),
        FULLSCREEN(e->e::fullScreenMode);

        private final Function<EditorActions, Runnable> function;

    BuildInFunctions(Function<EditorActions, Runnable> function) {
        this.function = function;
    }

    public static void addToMap(Map<String, Runnable> runnableMap, EditorActions e) {
        for(BuildInFunctions inFunctions: values()) {
            runnableMap.put(inFunctions.name(), inFunctions.function.apply(e));
        }
    }
}
