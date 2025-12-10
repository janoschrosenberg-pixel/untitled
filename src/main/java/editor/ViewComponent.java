package editor;

import javax.swing.*;

public abstract class ViewComponent extends JComponent implements EditorCommands{
    public abstract void appendChar(char sign);
    public abstract void delChar();

}
