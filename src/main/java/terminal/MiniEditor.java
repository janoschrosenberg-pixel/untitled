package terminal;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import editor.*;
import indexer.JavaFileIndex;
import indexer.JavaFileScanner;
import lsp.GoTo;
import lsp.JdtLsGotoDefinition;
import lsp.LSP;
import lsp.TypescriptLSP;
import stackmachine.Inter;
import stackmachine.StackUtils;
import stackmachine.Stackmachine;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static editor.MainFrame.tech;
import static terminal.CurrentBuffer.CURRENT_BUFFER;

public class MiniEditor implements EditorActions {

    private String statusBar="EDIT";
    private JavaFileIndex index = new JavaFileIndex();
    private CustomCommandMode customCommands = new CustomCommandMode();
    private Map<String, Buffer> buffers = new HashMap<>();
    private Map<Tech, LSP> lsps = new HashMap<>();
    private Inter inter;
    private boolean programRunning = true;
    private boolean updateAll = true;

    private final BoundedStack<Buffer> bufferHistory = new BoundedStack<>(100);

    private EditorCommands currentCommandMode =  CURRENT_BUFFER;
    {
        lsps.put(Tech.JAVA,  new JdtLsGotoDefinition());
        lsps.put(Tech.REACT, new TypescriptLSP());
    }

    private void loadBuffer (String fileName) throws IOException, InterruptedException {
        if (!buffers.containsKey(fileName)) {
            var buf = new Buffer(fileName);
            buf.setLines(getBufferFromDisk(fileName));
            buffers.put(fileName, buf);
            this.lsps.get(tech).openFile(fileName);
            buf.updateMethodInfos(true);
            buf.updateTokens(true);
        }
        var newBuffer = buffers.get(fileName);
        if(newBuffer != bufferHistory.peek()) {
            bufferHistory.push(newBuffer);
        }
        CURRENT_BUFFER.setBuffer(newBuffer);

    }

    private List<Line> getBufferFromDisk(String filename) throws IOException {
        if(tech == Tech.REACT) {
            return Utils.loadResourceFileReact(filename);
        }else{
            return Utils.loadResourceFile(filename);
        }
    }

    public  MiniEditor() throws IOException {
        Terminal terminal = new DefaultTerminalFactory().createTerminal();
        terminal.enterPrivateMode();
        terminal.setCursorVisible(true);

        terminal.addResizeListener((a,b)-> {
            CURRENT_BUFFER.setMaxLines(b.getRows()-1);
        });
        var b =terminal.getTerminalSize();
        CURRENT_BUFFER.setMaxLines(b.getRows()-1);


        CURRENT_BUFFER.setStrgAction(this::switchToCustomMode);

        inter = new Stackmachine(this);

        inter.runCommand("start");

        scanWorkspace(tech);
        var lsp = lsps.get(tech);
        var path = Path.of(lsp.getWorkspace());
        JavaFileScanner.scan(tech, path, index);

        updateStatusView();
        updateStatusBar(terminal);
        terminal.setCursorPosition(new TerminalPosition(0,0));

        while (programRunning) {
            var buffer = CURRENT_BUFFER.getCurentBuffer();

            int max = CURRENT_BUFFER.getMaxLines() + buffer.getTopLine();
            if(max>buffer.getLineSize()) {
                max = buffer.getLineSize();
            }

            // --- Draw text ---

            var selection = CURRENT_BUFFER.getCurentBuffer().getSelection();
            terminal.setCursorVisible(selection == null);
            terminal.setForegroundColor(TextColor.ANSI.WHITE_BRIGHT);
            terminal.setBackgroundColor(TextColor.ANSI.BLACK);


            if(updateAll) {
                terminal.clearScreen();
                for (int i = buffer.getTopLine(); i < max; i++) {
                    buffer.getLines().get(i).drawLineWithSyntaxHighlighting(selection, terminal, i, buffer.getTopLine());
                    updateAll = false;
                }
            }else{
                if(selection!= null) {
                    for (int i = buffer.getTopLine()+selection.fromLine(); i < selection.toLine()+1; i++) {
                        buffer.getLines().get(i).drawLineWithSyntaxHighlighting(selection, terminal, i, buffer.getTopLine());
                    }
                }else{
                    int i=buffer.getCursorRow();
                    buffer.getLines().get(i).drawLineWithSyntaxHighlighting(null, terminal, i, buffer.getTopLine());
                }
            }

            updateStatusBar(terminal);

            terminal.setCursorPosition(new TerminalPosition(buffer.getCursorCol(),buffer.getCursorRow()-buffer.getTopLine()));
            // --- Cursor ---
            terminal.flush();



            // --- Input ---
            KeyStroke key = terminal.readInput();
            if (key == null){

                continue;
            }

            if(key.isCtrlDown() && key.getCharacter() == ' ') {
                currentCommandMode.ctrlPressed();
                continue;
            }

            if (key.getKeyType() == KeyType.Escape) {
                currentCommandMode.esc();
            }

            if (key.getKeyType() == KeyType.Character) {
                currentCommandMode.appendChar(key.getCharacter());
            }

            if (key.getKeyType() == KeyType.Backspace) {
                currentCommandMode.delChar();
            }

            if (key.getKeyType() == KeyType.ArrowLeft) {
                //TODO
            }

            if (key.getKeyType() == KeyType.ArrowRight) {

            }
            if (key.getKeyType() == KeyType.ArrowUp) {

            }

            if (key.getKeyType() == KeyType.ArrowDown) {

            }


            if (key.getKeyType() == KeyType.Enter) {
                currentCommandMode.enter();
            }
        }

        try {
            if(this.lsps.get(tech).getWorkspace() != null) {
                this.lsps.get(tech).shutdownServer();
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        terminal.exitPrivateMode();
        terminal.close();
        System.exit(0);
    }


    private void updateStatusView(){
        setStatus(tech.name()+" "+currentCommandMode.showModus());
    }
    private void updateStatusBar(Terminal terminal) throws IOException {
        terminal.setForegroundColor(TextColor.ANSI.BLACK);
        terminal.setBackgroundColor(TextColor.ANSI.WHITE);
        terminal.setCursorPosition(new TerminalPosition(0,CURRENT_BUFFER.getMaxLines()));
        terminal.putString(this.statusBar);
    }

    public static void main(String[] args) throws IOException {
       new MiniEditor();
    }

    @Override
    public void loadFile(String file) {
        try {
            this.loadBuffer(file);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        updateAll = true;
    }

    @Override
    public void switchToCommandMode() {

    }

    @Override
    public void switchToEditorMode() {
        this.currentCommandMode =  CURRENT_BUFFER;
        updateStatusView();
    }

    @Override
    public void switchToCustomMode() {
        this.currentCommandMode = customCommands;
        updateStatusView();
    }

    @Override
    public void clearBuffer() {

    }

    @Override
    public void setStatus(String status) {
        statusBar = status;
    }

    @Override
    public void moveCursorUp(int amount) {
        CURRENT_BUFFER.moveCursorUp(amount);
    }

    @Override
    public void moveCursorDown(int amount) {
        CURRENT_BUFFER.moveCursorDown(amount);
    }

    @Override
    public void moveCursorLeft(int amount) {
        CURRENT_BUFFER.moveCursorLeft(amount);
    }

    @Override
    public void moveCursorRight(int amount) {
        CURRENT_BUFFER.moveCursorRight(amount);
    }

    @Override
    public void scrollUp(int amount) {

    }

    @Override
    public void scrollDown(int amount) {

    }

    @Override
    public void bind(String key, String mode, Runnable command) {
        this.customCommands.bind(key,mode, command);
    }

    @Override
    public void returnCommandContext() {
        this.customCommands.returnContext();
        updateStatusView();
    }

    @Override
    public void setEditorMode(String mode) {
        this.customCommands.setMode(mode);
        updateStatusView();
    }

    @Override
    public void saveBuffer(String file) {

    }

    @Override
    public void fullScreenMode() {

    }

    @Override
    public void toNextWord() {
        CURRENT_BUFFER.setCursorToNextToken();
    }

    @Override
    public void toPrevWord() {
        CURRENT_BUFFER.setCursorToPrevToken();
    }

    @Override
    public void toNextMethod() {
        CURRENT_BUFFER.nextMethod();
    }

    @Override
    public void toPrevMethod() {
        CURRENT_BUFFER.prevMethod();
    }

    @Override
    public void openMenu(String name) {

        if(!buffers.containsKey(name)) {
            var listMenu = new Buffer(name);
            listMenu.setEdited(true);
            buffers.put(name, listMenu);
        }

        bufferHistory.push(CURRENT_BUFFER.getCurentBuffer());
        CURRENT_BUFFER.setBuffer(buffers.get(name));

        var lines =  inter.getStackObjects().stream().map(o -> {
            Line line = new Line();
            line. text = new StringBuilder(o.toString());
         return line;
        }).toList();
        CURRENT_BUFFER.getCurentBuffer().setLines(lines);
    }



    @Override
    public void closeMenu() {
        var prevBuffer = bufferHistory.pop();
        CURRENT_BUFFER.setBuffer(prevBuffer);
        this.returnCommandContext();
    }


    @Override
    public void registerWorkspace(String path) {

    }

    @Override
    public void startLanguageServer() {

    }

    @Override
    public void findDefinition() {
        var cursorPosition = CURRENT_BUFFER.getCurentBuffer().getCursorPosition();
        int row = cursorPosition.line();
        int col = cursorPosition.col();
        try {
            GoTo dest= this.lsps.get(tech).findDefinition(row, col, CURRENT_BUFFER.getCurentBuffer().getFileName());
            if(dest != null) {
                loadFile(dest.file());
                CURRENT_BUFFER.getCurentBuffer().setCursorPosition(new CursorPosition(dest.row(), dest.col()));
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateTokens() {
        CURRENT_BUFFER.getCurentBuffer().updateTokens(true);
    }

    @Override
    public void exit() {
        programRunning = false;
    }

    @Override
    public void javaFiles2Stack() {
        index.snapshot(tech).forEach(inter::push);
    }

    @Override
    public void switchTech(String t) {
        tech = Tech.valueOf(t);

        var lsp = lsps.get(tech);

        if(lsp.getWorkspace() == null) {
            try {
                scanWorkspace(tech);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if(!index.hasTech(tech)) {
            try {
                var path = Path.of(lsp.getWorkspace());
                JavaFileScanner.scan(tech, path, index);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        updateStatusView();
    }

    @Override
    public void formatCode() {

    }

    @Override
    public String getCurrentTech() {
        return "";
    }

    @Override
    public void putSelectionOnStack() {
        String selection =   CURRENT_BUFFER.getCurentBuffer().getSelectedString();
        this.inter.push(selection);
    }

    @Override
    public void selectCurrentLine() {
        CURRENT_BUFFER.getCurentBuffer().selectCurrentLine();
    }

    private String getWorkspaceFile() {
        return "workspace_"+tech.name()+".txt";
    }
    private void scanWorkspace(Tech t) throws IOException {
        var lsp = lsps.get(t);
        StackUtils.readLines(getWorkspaceFile(), lsp::setWorkspace);
        try {
            lsp.startServer();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
