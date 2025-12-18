package editor;

import stackmachine.Inter;
import stackmachine.StackUtils;
import stackmachine.Stackmachine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class MainFrame extends JFrame implements EditorActions{
    private final Map<String,  EditorView> views = new HashMap<>();
    private EditorView current;
    private String currentFilename = "NEW_FILE";
    Inter stackmachine;
    private StringBuilder statusView = new StringBuilder();
    JLabel statusLabel = new JLabel();

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
        switchToEditorMode();
        revalidate();
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
    }

    @Override
    public void setEditorMode(String mode) {
        this.customCommandMode.setMode(mode);
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


    public MainFrame() throws IOException {

        this.customCommandMode = new CustomCommandMode();
        setLayout(new BorderLayout());
        stackmachine = new Stackmachine(this);
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
    }
}
