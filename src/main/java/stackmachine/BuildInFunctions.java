package stackmachine;

import java.util.Map;
import java.util.Stack;
import java.util.function.BiFunction;

public enum BuildInFunctions {
        CHANGE_EDITOR_MODE((e,s)-> ()-> e.setEditorMode(s.pop().toString())),
        PREV_MODE((e,s)->e::returnCommandContext),
        COMMAND_MODE((e, s)->e::switchToCommandMode),
        EDITOR_MODE((e, s)->e::switchToEditorMode),
        CURSOR_UP((e, s)->()->e.moveCursorUp(1)),
        CURSOR_DOWN((e, s)->()->e.moveCurserDown(1)),
        CURSOR_LEFT((e, s)->()->e.moveCurserLeft(1)),
        CURSOR_RIGHT((e, s)->()->e.moveCurserRight(1)),

        CURSOR_NEXT_TOKEN((e,s)->e::toNextWord),
        CURSOR_PREV_TOKEN((e,s)->e::toPrevWord),

        CURSOR_PREV_METHOD((e,s)->e::toPrevMethod),
        CURSOR_NEXT_METHOD((e,s)->e::toNextMethod),

        FULLSCREEN((e, s)->e::fullScreenMode);

        private final BiFunction<editor.EditorActions,Stack<Object>, Runnable> function;

    BuildInFunctions(BiFunction<editor.EditorActions,Stack<Object>, Runnable> function) {
        this.function = function;
    }

    public static void addToMap(Map<String, Runnable> runnableMap, editor.EditorActions e, Stack<Object> stack) {
        for(BuildInFunctions inFunctions: values()) {
            runnableMap.put(inFunctions.name(), inFunctions.function.apply(e, stack));
        }
    }
}
