package stackmachine;

import java.util.Map;
import java.util.Stack;
import java.util.function.BiFunction;

public enum BuildInFunctions {
        CHANGE_EDITOR_MODE((e,s)-> ()-> e.setEditorMode(s.pop().toString())),
        FIND_DEFINITION((e,s)->e::findDefinition),
        PREV_MODE((e,s)->e::returnCommandContext),
        COMMAND_MODE((e, s)->e::switchToCommandMode),
        EDITOR_MODE((e, s)->e::switchToEditorMode),
        CURSOR_UP((e, s)->()->e.moveCursorUp(1)),
        CURSOR_DOWN((e, s)->()->e.moveCursorDown(1)),
        CURSOR_LEFT((e, s)->()->e.moveCursorLeft(1)),
        CURSOR_RIGHT((e, s)->()->e.moveCursorRight(1)),

        CURSOR_NEXT_TOKEN((e,s)->e::toNextWord),
        CURSOR_PREV_TOKEN((e,s)->e::toPrevWord),

        CURSOR_PREV_METHOD((e,s)->e::toPrevMethod),
        CURSOR_NEXT_METHOD((e,s)->e::toNextMethod),

        OPEN_MENU((e,s)->

            ()-> {
                String menuName = s.pop().toString();
                e.openMenu(menuName);
                e.setEditorMode(menuName);
                e.switchToCustomMode();
            }
        ),



    REGISTER_WORKSPACE_AND_START_LANG_SERVER((e,s)->
            ()-> {
                String workspace = s.pop().toString();
                e.registerWorkspace(workspace);
                e.startLanguageServer();
            }
    ),
    SELECT_LINE((e,s)->e::selectCurrentLine),
    SELECTION_2_STACK((e,s)->e::putSelectionOnStack),
        JAVA_FILES_2_STACK((e,s) -> e::javaFiles2Stack),
        CLOSE_MENU((e,s)-> e::closeMenu),
        SWITCH_TECH((e, s) -> ()-> {
            String tech = s.pop().toString();
            e.switchTech(tech);
        }),
        FORMAT_CODE((e,s)->e::formatCode),
        GET_TECH((e, s)-> ()-> {
                    s.push( e.getCurrentTech());
        }),
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
