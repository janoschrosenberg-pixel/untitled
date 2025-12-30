package editor;

import parser.MethodScannerUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ScrollHandler {

    private List<MethodScannerUtil.MethodInfo> methodInfos = new ArrayList<>();

    private Selection selection = null;
    private int curserRow=0;

    public int getTopLine() {
        return topLine;
    }

    public int getCurserCol() {
        return curserCol;
    }

    public int getCurserRow() {
        return curserRow;
    }

    private int curserCol=0;
    private int topLine=0;


    private void revalidateScrollUp(){
        var diff = topLine-curserRow;
        if(diff>0) {
            topLine-=diff;
        }
    }

    public void delChar( List<Line> lines) {

        if(curserRow == 0 && curserCol == 0){
            return;
        }

        if(lines.size()-1 < curserRow){
            lines.add(curserRow, new Line());
        }

        if(shouldMerge(lines)){
            mergeLines(lines);
            revalidateScrollUp();

            return;
        }
        var currentLine = lines.get(curserRow);

        if(curserCol == 0) {
            var aboveLine = lines.get(curserRow-1);
            if (currentLine.isEmpty()) {
                lines.remove(curserRow);
                curserRow -= 1;
                curserCol = aboveLine.length();
            }else{

                if(aboveLine.isEmpty()) {
                    lines.remove(aboveLine);
                    curserRow -= 1;
                }
            }
            revalidateScrollUp();

            return;
        }

        moveCurserLeft(1, getCurrentLineSize(lines));
        if(currentLine.length()>curserCol){
            currentLine.deleteCharAt(curserCol);
        }

    }

    private boolean shouldMerge(List<Line> lines) {
        return  curserCol == 0 &&
                !lines.get(curserRow).isEmpty() &&
                curserRow > 0 &&
                !lines.get(curserRow-1).isEmpty();
    }

    private void mergeLines(List<Line> lines){
        var aboveLine = lines.get(curserRow-1);
        curserCol = aboveLine.length();
        aboveLine.append(lines.get(curserRow));
        lines.remove(curserRow);
        curserRow -= 1;
    }

    public void moveCurserRight(int amount){
        curserCol+=amount;
    }

    public void moveCurserDown(int amount, int maxLineSize, int heightThroughCharHeight){
        if (curserRow < maxLineSize - 1) {
            curserRow+=amount;
        }

        revalidateScollDown(heightThroughCharHeight);
    }

    private void revalidateScollDown(int heightThroughCharHeight) {
        var mayI = heightThroughCharHeight + topLine;
        var iAm = curserRow+1;
        var diff = iAm-mayI;

        if(diff>0) {
            topLine += diff;
        }
    }

    private void revalidateScollDown() {
        var diff = curserRow-topLine-3;
        if(diff>0) {
            topLine += diff;
        }
    }

    public void moveCurserLeft(int amount, int maxLines){
        if(curserCol == 0 && curserRow == 0) {
            return;
        }

        if ((curserCol-amount) >= 0) {
            curserCol-=amount;
        }else {
            moveCurserUp(1);
            curserCol = maxLines;
        }

    }

    public int getRelativeCourserRow(){
        return curserRow-topLine;
    }

    public void scrollUp() {
        if (topLine > 0) topLine--;
    }

    public void scrollDown(int max) {
        if (topLine < max - 1) topLine++;

    }

    public void moveCurserUp(int amount){
        if (curserRow > 0) {
            curserRow-=amount;
        }
        revalidateScrollUp();
    }

    public void appendChar(char sign, List<Line> lines) {
        if(lines.size()-1 < curserRow){
            lines.add(curserRow, new Line());
        }
        var line = lines.get(curserRow);
        while (line.length() < curserCol) {
            line.append(' ');
        }
        line.insert(curserCol, sign);
        moveCurserRight(1);
    }

    public int getCurrentLineSize(List<Line> lines){
        return lines.get(getCurserRow()).length();
    }

    public void enter(List<Line> lines, int heightThroughCharHeight) {
        Line currentLine = lines.get(curserRow);
        String subString = "";
        if(currentLine.length()>curserCol){
            subString  = currentLine.substring(curserCol);
            currentLine.delete(curserCol, currentLine.length());
        }

        lines.add(curserRow+1, new Line(subString));
        moveCurserDown(1,lines.size(), heightThroughCharHeight);
        curserCol = 0;
    }

    public void setCursorToPreviousToken(List<Line> lines){
        setCursorToPreviousToken(lines, this.curserRow, this.curserCol);
    }

    public void setCursorToNextToken(List<Line> lines, int relative){
        setCursorToNextToken(lines, this.curserRow, this.curserCol, relative);
    }

    private void handlePrevRow(List<Line> lines, int row) {
        if(row<0){
            return;
        }
        Line line = lines.get(row);
        var tokens = line.getTokenSkipWhitespace();
        if(tokens.isEmpty()) {
            handlePrevRow(lines, row-1);
            return;
        }

        var nextToken = tokens.getLast();

        this.curserCol = nextToken.start();
        this.curserRow = row;

        revalidateScrollUp();

        this.selection = new Selection(curserRow, curserCol, curserRow, 0,  nextToken.end());

    }
    private void handleNextRow(List<Line> lines, int row, int relative) {
        if(row>=lines.size()) {
            return;
        }

        Line line = lines.get(row);
        var tokens = line.getTokenSkipWhitespace();
        if(tokens.isEmpty()) {
            handleNextRow(lines, row+1, relative);
            return;
        }

        var nextToken = tokens.getFirst();

        this.curserCol = nextToken.start();
        this.curserRow = row;

        revalidateScollDown(relative);

        this.selection = new Selection(curserRow, curserCol, curserRow, 0,  nextToken.end());

    }


    private void setCursorToNextToken(List<Line> lines, int row, int col, int relative) {
        Line line = lines.get(row);
        var tokens = line.getTokenSkipWhitespace();

        if(tokens.isEmpty()) {
            handleNextRow(lines, row+1, relative);
            return;
        }

        var result = line.getCurrentTokenSkipWhitespace(col);

        var newIndex = result.index()+1;

        if(newIndex >= tokens.size()){
            handleNextRow(lines, row+1, relative);
            return;
        }

        var nextToken = tokens.get(newIndex);

        this.curserCol = nextToken.start();
        this.curserRow = row;
        this.selection = new Selection(curserRow, curserCol, curserRow, 0,  nextToken.end());
    }


    private void setCursorToPreviousToken(List<Line> lines, int row, int col) {
        Line line = lines.get(row);
        var tokens = line.getTokenSkipWhitespace();

        if(tokens.isEmpty()) {
            handlePrevRow(lines, row-1);
            return;
        }

        var result = line.getCurrentTokenSkipWhitespace(col);

        var newIndex = result.index()-1;

        if(newIndex<0){
            handlePrevRow(lines, row-1);
            return;
        }

        var nextToken = tokens.get(newIndex);

        this.curserCol = nextToken.start();
        this.curserRow = row;
        this.selection = new Selection(curserRow, curserCol, curserRow, 0 , nextToken.end());
    }

    public void updateMethodInfos(List<Line> lines){
        var code = Utils.mergeLines(lines);
        this.methodInfos = MethodScannerUtil.scan(code);
        this.methodInfos.sort(Comparator.comparingInt(MethodScannerUtil.MethodInfo::startLine));

    }

    public Selection getSelection() {
        return selection;
    }

    public void clearSelection() {
        this.selection = null;
    }


    public void setCursorToPrevMethodStart(List<Line> lines) {
        if(methodInfos.isEmpty()) {
            return;
        }
        Result<MethodScannerUtil.MethodInfo> info =  Utils.findRangeResult(this.methodInfos, curserRow+1);

        int nextIndex = info.index() - 1;

        MethodScannerUtil.MethodInfo next;
        if(nextIndex<0) {
            next =  info.object();
        }else{
            next = this.methodInfos.get(nextIndex);
        }

        curserRow = next.startLine() -1;
        curserCol = next.startColumn() -1;

        revalidateScrollUp();

        this.selection = new Selection(curserRow, curserCol, next.endLine()-1,  lines.get(next.bodyStartLine()-1).text.length(), next.endColumn());
    }

    public void setCursorToNextMethodStart(List<Line> lines) {
        if(methodInfos.isEmpty()) {
            return;
        }
         Result<MethodScannerUtil.MethodInfo> info =  Utils.findRangeResult(this.methodInfos, curserRow+1);

        int nextIndex = info.index() + 1;

        MethodScannerUtil.MethodInfo next;
        if(nextIndex>=this.methodInfos.size()) {
            next = info.object();
        }else{
            next = this.methodInfos.get(nextIndex);
        }
        curserRow = next.startLine() -1;
        curserCol = next.startColumn() -1;

        revalidateScollDown();
        this.selection = new Selection(curserRow, curserCol, next.endLine()-1,  lines.get(next.bodyStartLine()-1).text.length(), next.endColumn());


    }

    public void setRow(int row) {
        this.curserRow = row;
        revalidateScollDown();
    }

    public void setColumn(int column) {
        this.curserCol = column;
    }
}

