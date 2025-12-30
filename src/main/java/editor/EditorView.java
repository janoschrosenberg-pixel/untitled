package editor;

import formatter.CodeFormatter;
import stackmachine.Inter;

import java.awt.*;

import java.util.*;
import java.util.List;

public class EditorView extends ViewComponent {

    private List<Line> lines;

    private static final Stack<Menu> menuStack = new Stack<>();

    private static final Map<String, Menu> menuMap = new HashMap<>();

    static {
        menuMap.put("FILEMENU", new FileMenu());
        menuMap.put("LISTMENU", new ListMenu());
    }

    private final ScrollHandler scrollHandler = new ScrollHandler();


    private final EditorActions editorActions;

    public final static int lineHeight = 16;
    private final  int charHeight = lineHeight;
    Font monoFont = new Font(Font.MONOSPACED, Font.PLAIN, lineHeight);
    public EditorView(List<Line> lines, EditorActions editorActions, Inter stackmachine) {


        if(lines == null) {
            this.lines = new ArrayList<>();
            this.lines.add(new Line());
        }else{
            this.lines = lines;
        }

        this.editorActions = editorActions;
        scrollHandler.updateMethodInfos(this.lines);


        // menus
    for(var key:menuMap.keySet()) {
        menuMap.get(key).setInter(stackmachine);
    }
    }



    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if(!menuStack.isEmpty()) {
            menuStack.peek().paint(g);
            return;
        }

        g.setFont(monoFont);

        int y = 0;

        int visibleLines = getHeight() / lineHeight;

        FontMetrics fm = g.getFontMetrics();  // aktuelle Font
        int charWidth = fm.charWidth('M');    // Breite eines Zeichens


        g.setColor(Color.BLACK);
        g.fillRect(0,0, getWidth(),getHeight());
        var selection = scrollHandler.getSelection();
        if(selection != null) {
            g.setColor(Color.DARK_GRAY);
            var width = Math.abs(selection.toColumn()-selection.fromColumn()) * charWidth;
            var height = (selection.toLine()-selection.fromLine()+1) * lineHeight;

            g.fillRect(selection.fromColumn()*charWidth+30, (selection.fromLine()-scrollHandler.getTopLine()) * lineHeight+5, width, height);
        }
        for (int i = 0; i < visibleLines; i++) {
            int lineIndex = scrollHandler.getTopLine() + i;
            if (lineIndex >= lines.size())
                break;

            Line line = lines.get(lineIndex);


            g.setColor(Color.GREEN);
            g.fillRect(scrollHandler.getCurserCol()*charWidth+30,
                    (scrollHandler.getRelativeCourserRow()+1)*lineHeight,charWidth, 4 );

            
            line.drawText(g, 30, y + lineHeight, selection, lineIndex);

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

public void clearSelection() {
        scrollHandler.clearSelection();
        repaint();
}

    @Override
    public void enter() {
        scrollHandler.enter(lines, getVisibleRows());
        repaint();
    }

    @Override
    public void ctrlPressed() {
        voidUpdateTokensAndMenuInfos();
    }

    private void voidUpdateTokensAndMenuInfos() {
        scrollHandler.updateMethodInfos(lines);
        editorActions.updateTokens();
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
        scrollHandler.setCursorToPrevMethodStart(lines);
        repaint();
    }

    public List<String> getLines() {
        return lines.stream().map(Line::toString).toList();
    }


    public void setLines(List<Line> lines){
        this.lines = lines;
        repaint();
    }

    public List<Line> getRealLines() {
        return this.lines;
    }

    public void toNextWord() {
        scrollHandler.setCursorToNextToken(lines,getVisibleRows());
        repaint();
    }

    public void toPrevMethod() {
        scrollHandler.setCursorToPrevMethodStart(lines);
        repaint();
    }

    public void toNextMethod() {
        scrollHandler.setCursorToNextMethodStart(lines);
        repaint();
    }

    public void setRow(int row) {
        scrollHandler.setRow(row);
        repaint();
    }

    public void setColumn(int column) {
        scrollHandler.setColumn(column);
        repaint();
    }

    public void toPrevWord() {
        scrollHandler.setCursorToPreviousToken(lines);
        repaint();
    }

    public void openMenu(String name) {
        menuStack.push( menuMap.get(name));
        repaint();
    }

    public void sendMenuCommand(String command) {
        if(!menuStack.isEmpty()) {
            menuStack.peek().fireMenuCommand(command);
        }
        repaint();
    }

    public void closeMenu() {
        if(!menuStack.isEmpty()) {
            menuStack.pop();
        }
        repaint();
    }

    public void registerMenuFunction(String menuName,String name, String function) {
        menuMap.get(menuName).registerFunction(name, function);
    }

    public int getCurrentSelectedRow() {
        return this.scrollHandler.getCurserRow();
    }

    public int getCurrentSelectedColumn() {
        return this.scrollHandler.getCurserCol();
    }

    public void formatCode() {
        if(MainFrame.tech == Tech.REACT){
            CodeFormatter.formatCode(lines);
        }
        voidUpdateTokensAndMenuInfos();
    }
}