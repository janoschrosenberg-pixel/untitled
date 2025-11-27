package main;

import tokenizer.Token;
import tokenizer.Tokenizer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Line {
    public final StringBuilder text;
    public List<Token> tokens;

    public Line(String text) {
        this.text = new StringBuilder(text);
        tokens = Tokenizer.tokenize(text);
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
        tokens = Tokenizer.tokenize(text.toString());
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public void drawText(Graphics g, int x, int y) {
        String line = toString();

        for (Token t : tokens) {
            String frag = line.substring(t.start(), t.end());
            g.setColor(getColorForType(t.type()));

            g.drawString(frag, x, y);
            x += g.getFontMetrics().stringWidth(frag);
        }
    }

    private Color getColorForType(Tokenizer type) {
      return  switch (type){
          case NUMBER ->  Color.ORANGE;
          case IDENTIFIER -> Color.WHITE;
          case CUSTOM -> Color.PINK;
        };
    }

}
