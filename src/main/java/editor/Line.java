package editor;

import tokenizer.Token;
import tokenizer.Tokenizer;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;





public class Line {

    public final StringBuilder text;
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

    public void drawText(Graphics g, int x, int y, Selection selection,int index) {
        String line = toString();

        if((tokens.isEmpty() && !this.text.isEmpty()) || (MainFrame.tech == Tech.REACT && MainFrame.editMode)) {
            g.drawString(this.text.toString(), x, y);
            return;
        }
        for (Token t : tokens) {
            String frag = line.substring(t.start(), t.end());
            Color textColor = t.type().getTextColor();

            int size = g.getFontMetrics().stringWidth(frag);
            if(selection != null
                    && selection.fromLine() == index
                    && index == selection.toLine() && t.equalsSelection(selection)) {

                Color background = t.type().getTextColor();
                textColor = t.type().getBackgroundColor();
                g.setColor(background);
                g.fillRect(x, y - 13, size, EditorView.lineHeight);
            }


            g.setColor(textColor);
            g.drawString(frag, x, y);

            x += size;
        }
    }


    public Result<Token> getCurrentTokenSkipWhitespace(int column) {
      return  Utils.findIntervalWithIndex(getTokenSkipWhitespace(), column, Token::start);
    }

    public List<Token> getTokenSkipWhitespace() {
        return tokens.stream().filter(Token::noWhitespace).toList();
    }

}
