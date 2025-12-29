package editor;

import indexer.JavaFileIndex;
import indexer.JavaFileScanner;
import lsp.GoTo;
import lsp.JdtLsGotoDefinition;
import lsp.LSP;
import lsp.TypescriptLSP;
import stackmachine.Inter;
import stackmachine.StackUtils;
import stackmachine.Stackmachine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainFrame extends JFrame implements EditorActions{
    private final Map<String,  EditorView> views = new HashMap<>();
    private EditorView current;
    private String currentFilename = "NEW_FILE";
    private JavaFileIndex index = new JavaFileIndex();
    Inter stackmachine;
    private StringBuilder statusView = new StringBuilder();
    JLabel statusLabel = new JLabel();

    private Tech tech = Tech.JAVA;
    private Map<Tech, LSP> lsps = new HashMap<>();

    {
        lsps.put(Tech.JAVA,  new JdtLsGotoDefinition());
        lsps.put(Tech.REACT, new TypescriptLSP());
    }

    private EditorCommands commandMode;

            private final CustomCommandMode customCommandMode;

    private final CommandView commandView;

    public void loadFile(String path)  {
        EditorView newView;

        if(views.containsKey(path)){
            newView = views.get(path);
        }else{
            try {
                newView = new EditorView(Utils.loadResourceFile(path), this, stackmachine);
            } catch (IOException e) {
                e.printStackTrace();
               return;
            }
            views.put(path, newView);
        }

        currentFilename = path;

        if(current != null) {
            remove(current);
        }

        add( newView,BorderLayout.CENTER);
        current = newView;
        switchToCustomMode();
        revalidate();
        try {
            this.lsps.get(tech).openFile(path);
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void switchToCommandMode() {
        commandMode = commandView;
        commandView.setVisible(true);
        updateStatusView();
    }

    @Override
    public void switchToEditorMode() {
        commandMode = current;
        commandView.setVisible(false);
        current.clearSelection();
        updateStatusView();
        revalidate();
    }

    @Override
    public void  switchToCustomMode() {
        commandMode = customCommandMode;
        updateStatusView();
    }

    @Override
    public void clearBuffer() {
        if(current != null) {
            remove(current);
        }

        current = new EditorView(null, this, stackmachine);
        add( current,BorderLayout.CENTER);
        revalidate();
    }

    private void updateStatusView(){
        setStatus(commandMode.showModus());
    }

    @Override
    public void setStatus(String status) {
        this.statusView = new StringBuilder(status);
        this.statusLabel.setText(statusView.toString());
    }

    @Override
    public void moveCursorUp(int amount) {
        this.current.moveCurserUp(amount);
    }

    @Override
    public void moveCurserDown(int amount) {
        this.current.moveCurserDown(amount);
    }

    @Override
    public void moveCurserLeft(int amount) {
        this.current.moveCurserLeft(amount);
    }

    @Override
    public void moveCurserRight(int amount) {
        this.current.moveCurserRight(amount);
    }

    @Override
    public void scrollUp(int amount) {

    }

    @Override
    public void scrollDown(int amount) {

    }

    @Override
    public void bind(String key, String mode, Runnable command) {
        this.customCommandMode.bind(key,mode, command);
    }

    @Override
    public void returnCommandContext() {
        this.customCommandMode.returnContext();
        updateStatusView();
    }

    @Override
    public void setEditorMode(String mode) {
        this.customCommandMode.setMode(mode);
        updateStatusView();
    }

    @Override
    public void saveBuffer(String file) {
        if(file!=null && !views.containsKey(file)) {
            views.put(file, current);
            currentFilename = file;
        }
        try {
            StackUtils.writeLinesToFile(current.getLines(), currentFilename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    @Override
    public void fullScreenMode() {
        dispose();
        setUndecorated(true);  // Entfernt Titelbar / Rahmen
        setResizable(false);

        // Fullscreen-Modus aktivieren
        GraphicsDevice gd = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();

        gd.setFullScreenWindow(this);
        setVisible(true);
    }

    @Override
    public void toNextWord() {
        this.current.toNextWord();
    }

    @Override
    public void toPrevWord() {
    this.current.toPrevWord();
    }

    @Override
    public void toNextMethod() {
        this.current.toNextMethod();
    }

    @Override
    public void toPrevMethod() {
        this.current.toPrevMethod();
    }

    @Override
    public void openMenu(String name) {
        this.current.openMenu(name);
    }

    @Override
    public void sendMenuCommand(String command) {
        this.current.sendMenuCommand(command);
    }

    @Override
    public void closeMenu() {
        this.current.closeMenu();
        this.returnCommandContext();
    }

    @Override
    public void registerMenuFunction(String menuName, String name, String function) {
        this.current.registerMenuFunction( menuName,name, function);
    }

    @Override
    public void registerKeyListener(String mode, String function) {
        this.customCommandMode.registerListener(mode, function);
    }

    @Override
    public void registerWorkspace(String path) {
        try {
            StackUtils.writeLinesToFile(List.of(path), getWorkspaceFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        lsps.get(tech).setWorkspace(path);
    }

    private String getWorkspaceFile() {
        return "workspace_"+tech.name()+".txt";
    }

    @Override
    public void startLanguageServer() {
        try {
            lsps.get(tech).startServer();
        } catch (IOException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void findDefinition() {
       int row = this.current.getCurrentSelectedRow();
       int col = this.current.getCurrentSelectedColumn();
        try {
            GoTo dest= this.lsps.get(tech).findDefinition(row, col, this.currentFilename);

            if(dest != null) {
                loadFile(dest.file());
                this.current.setRow(dest.row());
                this.current.setColumn(dest.col());
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void exit() {
        try {
            if(this.lsps.get(tech).getWorkspace() != null) {
                this.lsps.get(tech).shutdownServer();
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.exit(0);
    }

    @Override
    public void javaFiles2Stack() {
        index.snapshot(tech).forEach(stackmachine::push);
    }

    @Override
    public void switchTech(String tech) {

    }


    public MainFrame() throws IOException {

        this.customCommandMode = new CustomCommandMode();
        setLayout(new BorderLayout());
        stackmachine = new Stackmachine(this);
        this.customCommandMode.setStackMachine(this.stackmachine);
        clearBuffer();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setSize(800 , 600);

       commandView = new CommandView(this, stackmachine);
       this.commandMode = commandView;
       updateStatusView();
        addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_CONTROL) {
                    commandMode.ctrlReleased();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE){
                    commandMode.delChar();
                }

                if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                    commandMode.enter();
                }

                if(e.getKeyCode() == KeyEvent.VK_CONTROL) {
                    commandMode.ctrlPressed();
                }

                if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    commandMode.esc();
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (Character.isLetter(c) || Character.isDigit(c) ||
                        List.of('.', ';', ' ', '.', ':', '-', '\'', '_', '\"')
                                .contains(c)) {
                    commandMode.appendChar(c);
                }
            }
        });

        add(this.statusLabel, BorderLayout.NORTH);
        add(commandView, BorderLayout.SOUTH);

        setVisible(true);
        stackmachine.runCommand("start");

        for(Tech t:Tech.values()){
            scanWorkspace(t);
        }
    }

    private void scanWorkspace(Tech t) throws IOException {
        var lsp = lsps.get(t);
        StackUtils.readLines(getWorkspaceFile(), lsp::setWorkspace);
        try {
            lsp.startServer();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        var path = Path.of(lsp.getWorkspace());
        JavaFileScanner.scan(t, path, index);
    }
}
