package main;

import stackmachine.Inter;
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
                newView = new EditorView(Utils.loadResourceFile(path), this);
            } catch (IOException e) {
                e.printStackTrace();
               return;
            }
            views.put(path, newView);
        }

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
    public synchronized void switchToEditorMode() {
        commandMode = current;
        commandView.setVisible(false);
        updateStatusView();
        revalidate();
    }

    @Override
    public synchronized void  switchToCustomMode() {
        commandMode = customCommandMode;
        updateStatusView();
    }

    @Override
    public void clearBuffer() {
        if(current != null) {
            remove(current);
        }


        current = new EditorView(null, this);
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
    public void bind(char key, Runnable command) {
        this.customCommandMode.bind(key, command);
    }


    public MainFrame()  {

        this.customCommandMode = new CustomCommandMode(this);
        setLayout(new BorderLayout());
        clearBuffer();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setSize(800 , 600);
        Inter stackmachine = new Stackmachine(this);
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
    }
}
