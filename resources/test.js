import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

public class Tester {
    public static void main(String[] args) throws IOException {
    JFrame frame = new JFrame();

    EditorView editorView =  new EditorView(Utils.loadResourceFile("test.js"));

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    frame.add(
        editorView
    );
    frame.setSize(400 , 400);
    KeyListener keyListener = new KeyAdapter() {
    @Override
    public void keyPressed(KeyEvent e) {

    if(e.getKeyCode()==KeyEvent.VK_UP){
    editorView.scrollUp();
}
if(e.getKeyCode()==KeyEvent.VK_DOWN){
    editorView.scrollDown();
}
}
};
frame.addKeyListener(keyListener);
frame.setVisible(true);
}
}
