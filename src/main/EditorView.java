package main;

import tokenizer.Tokenizer;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class EditorView extends ViewComponent {

    private final List<Line> lines;
    private int topLine = 0;

    private int curserRow = 0;
    private int curserCol = 0;

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



        g.setColor(Color.DARK_GRAY);
        g.fillRect(0,0, getWidth(),getHeight());
        for (int i = 0; i < visibleLines; i++) {
            int lineIndex = topLine + i;
            if (lineIndex >= lines.size())
                break;

            Line line = lines.get(lineIndex);


            g.setColor(Color.CYAN);
            g.drawRect(curserCol*charWidth+30, (curserRow-topLine)*charHeight+5,charWidth, charHeight );

            g.setColor(lineIndex==curserRow?Color.PINK:Color.WHITE);

            line.drawText(g, 30, y + lineHeight);
            g.setColor(lineIndex==curserRow?Color.GREEN:Color.lightGray);
            g.drawString((lineIndex+1)+"", 0, y + lineHeight);
            y += lineHeight;
        }
    }

    public void scrollUp() {
        if (topLine > 0) topLine--;
        repaint();
    }

    public void scrollDown() {
        if (topLine < lines.size() - 1) topLine++;
        repaint();
    }

    public void moveCurserUp(int amount){
        if (curserRow > 0) {
            curserRow-=amount;
        }
        revalidateScrollUp();
        repaint();
    }

    private void revalidateScrollUp(){
        var diff = topLine-curserRow;
        if(diff>0) {
            topLine-=diff;
        }
    }

    public void moveCurserDown(int amount){
        if (curserRow < lines.size() - 1) {
            curserRow+=amount;
        }

        var mayI =  (getHeight()/charHeight)+topLine;
        var iAm = curserRow+1;
        var diff = iAm-mayI;

        if(diff>0) {
            topLine += diff;
        }

        repaint();
    }

    public void moveCurserLeft(int amount){
        if(curserCol == 0 && curserRow == 0) {
            return;
        }

        if ((curserCol-amount) >= 0) {
            curserCol-=amount;
        }else {
            moveCurserUp(1);
            curserCol = getCurrentLineSize();
        }
        repaint();
    }

    public int getCurrentLineSize(){
        return this.lines.get(curserRow).length();
    }

    public void moveCurserRight(int amount){


            curserCol+=amount;


        repaint();
    }

    public char getCharAtCursor(){
       return lines.get(curserRow+topLine).charAt(curserCol);
    }

    @Override
    public void appendChar(char sign) {
        if(lines.size()-1 < curserRow){
            lines.add(curserRow, new Line());
        }
        var line = lines.get(curserRow);
        while (line.length() < curserCol) {
            line.append(' ');
        }
        line.insert(curserCol, sign);
        moveCurserRight(1);
        repaint();
    }

    @Override
    public void delChar() {

        if(curserRow == 0 && curserCol == 0){
            return;
        }

        if(lines.size()-1 < curserRow){
            lines.add(curserRow, new Line());
        }

        if(shouldMerge()){
            mergeLines();
            revalidateScrollUp();
            repaint();
            return;
        }
        var currentLine = lines.get(curserRow);

        if(curserCol == 0) {
            if (currentLine.isEmpty()) {
                lines.remove(curserRow);
            }else{
                var aboveLine = lines.get(curserRow-1);
                if(aboveLine.isEmpty()) {
                    lines.remove(aboveLine);
                    curserRow -= 1;
                }
            }
            revalidateScrollUp();
            repaint();
            return;
        }


        moveCurserLeft(1);
        if(currentLine.length()>curserCol){
            currentLine.deleteCharAt(curserCol);
        }

        repaint();
    }

    private boolean shouldMerge() {
     return  curserCol == 0 &&
             !lines.get(curserRow).isEmpty() &&
             curserRow > 0 &&
             !lines.get(curserRow-1).isEmpty();
    }

    private void mergeLines(){
        var aboveLine = lines.get(curserRow-1);
        curserCol = aboveLine.length();
        aboveLine.append(lines.get(curserRow));
        lines.remove(curserRow);
        curserRow -= 1;
    }

    @Override
    public void enter() {
        Line currentLine = lines.get(curserRow);
        String subString = "";
        if(currentLine.length()>curserCol){
            subString  = currentLine.substring(curserCol);
            currentLine.delete(curserCol, currentLine.length());
        }

        lines.add(curserRow+1, new Line(subString));
        moveCurserDown(1);
        curserCol = 0;
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
        System.out.println( Tokenizer.tokenize(lines.get(curserRow).toString()));
    }

    public List<String> getLines() {
        return lines.stream().map(Line::toString).toList();
    }

}