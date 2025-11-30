package main;

import parser.MethodScannerUtil;
import tokenizer.Tokenizer;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class EditorView extends ViewComponent {

    private final List<Line> lines;

    private ScrollHandler scrollHandler = new ScrollHandler();

    private final  int charHeight = 18;
    private final EditorActions editorActions;

    private final int lineHeight = 18;

    Font customFont;
    {
        InputStream is = getClass().getClassLoader().getResourceAsStream("Courier Prime Code.ttf");

        // Font erzeugen
        try {
            assert is != null;
            customFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(16f);
        } catch (FontFormatException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public EditorView(List<Line> lines, EditorActions editorActions) {


        if(lines == null) {
            this.lines = new ArrayList<>();
            this.lines.add(new Line());
        }else{
            this.lines = lines;
        }

        this.editorActions = editorActions;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int y = 0;

        int visibleLines = getHeight() / lineHeight;
        g.setFont(customFont);
        FontMetrics fm = g.getFontMetrics();  // aktuelle Font
        int charWidth = fm.charWidth('M');    // Breite eines Zeichens

        g.setColor(Color.BLACK);
        g.fillRect(0,0, getWidth(),getHeight());
        for (int i = 0; i < visibleLines; i++) {
            int lineIndex = scrollHandler.getTopLine() + i;
            if (lineIndex >= lines.size())
                break;

            Line line = lines.get(lineIndex);


            g.setColor(Color.CYAN);
            g.drawRect(scrollHandler.getCurserCol()*charWidth+30,
                    (scrollHandler.getRelativeCourserRow())*charHeight+5,charWidth, charHeight );

            g.setColor(lineIndex==scrollHandler.getCurserRow()?Color.PINK:Color.WHITE);

            line.drawText(g, 30, y + lineHeight);
            g.setColor(lineIndex==scrollHandler.getCurserRow()?Color.GREEN:Color.lightGray);
            g.drawString((lineIndex+1)+"", 0, y + lineHeight);
            y += lineHeight;
        }
    }

    public void scrollUp() {
        scrollHandler.scrollUp();
        repaint();
    }

    public void scrollDown() {
        scrollHandler.scrollDown(lines.size());
        repaint();
    }

    public void moveCurserUp(int amount){
        scrollHandler.moveCurserUp(amount);
        repaint();
    }


    private int getVisibleRows() {
        return getHeight()/charHeight;
    }
    public void moveCurserDown(int amount){
        scrollHandler.moveCurserDown(amount, lines.size(), getVisibleRows());
        repaint();
    }

    public void moveCurserLeft(int amount){
        scrollHandler.moveCurserLeft(amount, getCurrentLineSize());
        repaint();
    }

    public int getCurrentLineSize(){
        return this.lines.get(scrollHandler.getCurserRow()).length();
    }

    public void moveCurserRight(int amount){
        scrollHandler.moveCurserRight(amount);
        repaint();
    }

    @Override
    public void appendChar(char sign) {
        scrollHandler.appendChar(sign, lines);
        repaint();
    }

    @Override
    public void delChar() {
        scrollHandler.delChar(lines);
        repaint();
    }



    @Override
    public void enter() {
        scrollHandler.enter(lines, getVisibleRows());
        repaint();
    }

    @Override
    public void ctrlPressed() {
        editorActions.switchToCustomMode();
    }

    @Override
    public void ctrlReleased() {

    }

    @Override
    public String showModus() {
        return "Editor Mode";
    }

    @Override
    public void esc() {
        var code = Utils.mergeLines(lines);
        System.out.println(code);
        var methods = MethodScannerUtil.scan(code);

        for (var m : methods) {
            System.out.println(m);
        }

    }

    public List<String> getLines() {
        return lines.stream().map(Line::toString).toList();
    }

}