package main;

import stackmachine.Inter;
import stackmachine.Stackmachine;

import javax.swing.*;
import java.awt.*;

public class CommandView extends ViewComponent{

    private final Inter stackmachine;
    private StringBuilder text = new StringBuilder();
    JLabel label = new JLabel();

    EditorActions editorActions;

    public CommandView (EditorActions editorActions, Inter stackmachine) {
        this.stackmachine = stackmachine;
        setLayout(new FlowLayout());
        this.editorActions = editorActions;

        label.setText(text.toString());
        add(label);
    }


    @Override
    public void appendChar(char sign) {
        text.append(sign);
        label.setText(text.toString());
    }

    @Override
    public void delChar() {
        text.deleteCharAt(text.length()-1);
        label.setText(text.toString());
    }

    @Override
    public void enter() {
        stackmachine.executeCommand(text.toString());
        text = new StringBuilder();
        label.setText(text.toString());
    }

    @Override
    public void ctrlPressed() {
    }

    @Override
    public void ctrlReleased() {

    }

    @Override
    public String showModus() {
        return "Command Mode";
    }

    @Override
    public void esc() {
        editorActions.switchToEditorMode();
    }
}
