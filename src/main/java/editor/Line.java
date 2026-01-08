package editor;

import com.googlecode.lanterna.SGR;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.terminal.Terminal;

import tokenizer.Token;
import tokenizer.Tokenizer;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Line {

    public  StringBuilder text;
    public List<Token> tokens;

    public void setTokens(List<Token> tokens){
        this.tokens = tokens;
    }

    public Line(String text) {
        this.text = new StringBuilder(text);
        tokens = Tokenizer.tokenize(text);
    }

    public Line(String text, List<Token> tokens) {
        this.tokens = tokens;
        this.text = new StringBuilder(text);
    }

    public Line() {
        this.text = new StringBuilder();
        tokens = new ArrayList<>();
    }


    public int length() {
        return text.length();
    }

    public String toString() {
        return text.toString();
    }

    public boolean isEmpty() {
        return text.isEmpty();
    }

    public void deleteCharAt(int pos) {
        text.deleteCharAt(pos);
        reparse();
    }

    public char charAt(int index) {
        return text.charAt(index);
    }

    // Mutationen:
    public void insert(int pos, char c) {
        text.insert(pos, c);
        reparse();
    }

    public void insert(int pos, String s) {
        text.insert(pos, s);
        reparse();
    }

    public void delete(int start, int end) {
        text.delete(start, end);
        reparse();
    }

    public String substring(int offset) {
        return text.substring(offset);
    }

    public void append(char c) {
        text.append(c);
        reparse();
    }

    public void append(Line line) {
        text.append(line.text);
        reparse();
    }

    public void append(String string) {
        text.append(string);
        reparse();
    }

    public void replace(int start, int end, String s) {
        text.replace(start, end, s);
        reparse();
    }

    private void reparse() {
        if(MainFrame.tech == Tech.JAVA) {
            tokens = Tokenizer.tokenize(text.toString());
        }
    }

    public List<Token> getTokens() {
        return tokens;
    }

    private Token findTokenAt(List<Token> tokens, int column) {
        for (Token t : tokens) {
            if (column >= t.start() && column < t.end()) {
                return t;
            }
        }
        return null;
    }
    private void drawLineWithSyntaxHighlighting(
            List<Token> tokens,
            String currentLine,
            int topLine,
            int currentIndex,
            Selection selection,
            Terminal terminal
    ) throws IOException {

        int screenY = currentIndex - topLine;
        if (screenY < 0) return;

        for (int column = 0; column < currentLine.length(); column++) {

            char ch = currentLine.charAt(column);

            Token token = findTokenAt(tokens, column);

            // -------- Default Farben --------
            TextColor fg = TextColor.ANSI.WHITE;
            TextColor bg;
            SGR[] style = new SGR[0];

            if (token != null) {
                fg = token.type().getForeground();
            }

            // -------- Selection überschreibt Background --------
            if (isSelected(selection, currentIndex, column)) {

                    bg = TextColor.ANSI.BLUE;

                fg = TextColor.ANSI.WHITE;
                style = new SGR[]{SGR.BOLD};
            }else{
                 bg = TextColor.ANSI.BLACK;
            }

            terminal.setForegroundColor(fg);
            terminal.setBackgroundColor(bg);

            for (SGR sgr : style) {
                terminal.enableSGR(sgr);
            }

            terminal.setCursorPosition(column, screenY);
            terminal.putCharacter(ch);

            for (SGR sgr : style) {
                terminal.disableSGR(sgr);
            }
        }
    }

    private boolean isSelected(Selection sel, int line, int column) {
        if (sel == null) return false;

        if (line < sel.fromLine() || line > sel.toLine()) return false;

        if (sel.fromLine() == sel.toLine()) {
            return column >= sel.fromColumn() && column < sel.toColumn();
        }

        if (line == sel.fromLine()) {
            return column >= sel.fromColumn();
        }

        if (line == sel.toLine()) {
            return column < sel.toColumn();
        }

        return true;
    }

    public void drawLineWithSyntaxHighlighting( Selection selection, Terminal terminal, int i, int  topLine) throws IOException {
        drawLineWithSyntaxHighlighting(this.tokens, this.text.toString(), topLine, i, selection, terminal);
    }

    public Result<Token> getCurrentTokenSkipWhitespace(int column) {
      return  Utils.findIntervalWithIndex(getTokenSkipWhitespace(), column, Token::start);
    }

    public List<Token> getTokenSkipWhitespace() {
        return tokens.stream().filter(Token::noWhitespace).toList();
    }

}
