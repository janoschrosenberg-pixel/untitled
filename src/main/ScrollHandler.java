package main;

import tokenizer.Token;
import tokenizer.Tokenizer;

import java.util.List;

public class ScrollHandler {
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
        var mayI = heightThroughCharHeight +topLine;
        var iAm = curserRow+1;
        var diff = iAm-mayI;

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

    private void setCursorToPreviousToken(List<Line> lines, int currentRow, int currentColumn) {

        Line line = lines.get(currentRow);
        var tokens = line.getTokens().stream().filter(t -> t.type() != Tokenizer.WHITESPACE).toList();
        int tokenId = line.getCurrentTokenIdSkipWhiteSpace(currentColumn);

        if(!tokens.isEmpty()) {

            // is on white space
            if(tokenId < 0) {
                for (int i = tokens.size()-1 ; i>=0; i--) {
                    Token currentToken = tokens.get(i);
                    if(currentToken.isBetween(currentColumn)) {
                        this.curserCol = currentToken.start();
                        this.curserRow = currentRow;
                        return;
                    }
                }

                if(curserRow>0) {
                    setCursorToPreviousToken(lines, currentRow -1, currentColumn);
                }
            } else {
                this.curserCol = tokens.get(tokenId).start();
                this.curserRow = currentRow;
            }


        }else{
            if(curserRow>0) {
                setCursorToPreviousToken(lines, currentRow -1, currentColumn);
            }
        }
    }

    public void setCursorToPreviousToken(List<Line> lines) {
        Line line = lines.get(curserRow);
        var tokens = line.getTokens().stream().filter(t -> t.type() != Tokenizer.WHITESPACE).toList();
        if(!tokens.isEmpty()) {
            int id;
            var currentCurserCol = curserCol;
            while((id=line.getCurrentTokenId(currentCurserCol))<0 && currentCurserCol>0){
                currentCurserCol-=1;
            }



            var last = tokens.getLast();
            if(last.end()<=curserCol) {
                curserCol = last.start();
                return;
            }
            if (id > 0 ) {
                curserCol = tokens.get(id - 1).start();
                return;
            }
        }

        int counter = curserRow-1;
        while ( counter > 0 ) {
            Line prevLine = lines.get(counter);
            if(!prevLine.tokens.isEmpty()) {
                curserCol = prevLine.tokens.getLast().start();
                curserRow = counter;
                revalidateScrollUp();
                return;
            }
            counter--;
        }

    }

    public void setCurserToNextToken(List<Line> lines,int heightThroughCharHeight) {
       Line line = lines.get(curserRow);
        var tokens = line.getTokens();

       if(!tokens.isEmpty()) {
           int id = line.getCurrentTokenId(curserCol);
           if (id >= 0 && id + 1 < tokens.size()) {
               curserCol = tokens.get(id + 1).start();
               return;
           }
       }

       int counter = 1;
       while (lines.size()> curserRow + counter ) {
           Line nextLine = lines.get(curserRow + counter);
           if(!nextLine.tokens.isEmpty()) {
               curserCol = nextLine.tokens.getFirst().start();
               curserRow+= counter;
               revalidateScollDown(heightThroughCharHeight);
               return;
           }
           counter++;
       }
    }

}
