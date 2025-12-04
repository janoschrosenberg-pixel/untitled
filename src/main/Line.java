package main;

import tokenizer.Token;
import tokenizer.Tokenizer;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static main.ColorUtils.getOptimalContrastColor;
import static main.EditorView.lineHeight;



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

    public void drawText(Graphics g, int x, int y, Selection selection,int index) {
        String line = toString();

        for (Token t : tokens) {
            String frag = line.substring(t.start(), t.end());
            Color textColor = getColorForType(t.type());

            int size = g.getFontMetrics().stringWidth(frag);
            if(selection != null
                    && selection.fromLine() == index
                    && index == selection.toLine() && t.equalsSelection(selection)) {

                Color background = textColor;
                textColor = getOptimalContrastColor(textColor);
                g.setColor(background);
                g.fillRect(x, y - 13, size, lineHeight);
            }


            g.setColor(textColor);
            g.drawString(frag, x, y);

            x += size;
        }
    }

    private Color getColorForType(Tokenizer type) {
      return  switch (type){
          case ARITHMETIC -> Color.decode("#00f0ff");
          case COLON -> Color.PINK;
          case LOGICAL -> Color.MAGENTA;
          case ROUND_BRACKET -> Color.BLUE;
          case MODIFIER -> Color.ORANGE;
          case COMPARE -> Color.YELLOW;
          case NUMBER ->  Color.decode("#fe00ff");
          case IDENTIFIER, WHITESPACE -> Color.LIGHT_GRAY;
          case KEYWORD, CHAR -> Color.decode("#f889e8");
          case STATIC -> Color.GREEN;
          case BRACKET -> Color.RED;
          case STRING ->  Color.decode("#f6b2d7");
          case COMMENT -> Color.GRAY;
          case SYMBOL -> Color.WHITE;
          case OPERATOR -> Color.decode("#f9e3e8");
          case UNKNOWN -> Color.CYAN;
        };
    }


    public Result<Token> getCurrentTokenSkipWhitespace(int column) {
      return  Utils.findIntervalWithIndex(getTokenSkipWhitespace(), column, Token::start);
    }

    public List<Token> getTokenSkipWhitespace() {
        return tokens.stream().filter(Token::noWhitespace).toList();
    }

}
