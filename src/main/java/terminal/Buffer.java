package terminal;

import editor.Line;
import editor.MainFrame;
import editor.Selection;
import editor.Tech;
import editor.Utils;


import java.util.List;

/**
 * Facade for buffer operations.
 * Delegates responsibilities to specialized components:
 * - BufferState: content and metadata
 * - ViewportManager: cursor position and scrolling
 * - TextOperations: text editing
 * - TokenNavigator: token-based navigation
 */
public class Buffer {
    private final BufferState state;
    private final ViewportManager viewport;
    private final TextOperations textOps;
    private final TokenNavigator navigator;

    public Buffer(String fileName) {
        this.state = new BufferState(fileName);
        this.viewport = new ViewportManager(state);
        this.textOps = new TextOperations(state, viewport);
        this.navigator = new TokenNavigator(state, viewport);
    }

    // ==================== BufferState Delegation ====================

    public String getFileName() {
        return state.getFileName();
    }

    public void setFileName(String fileName) {
        state.setFileName(fileName);
    }

    public boolean isEdited() {
        return state.isEdited();
    }

    public void setEdited(boolean edited) {
        state.setEdited(edited);
    }

    public List<Line> getLines() {
        return state.getLines();
    }

    public void setLines(List<Line> lines) {
        state.setLines(lines);
    }

    public int getLineSize() {
        return state.getLineCount();
    }

    public Selection getSelection() {
        return state.getSelection();
    }

    // ==================== ViewportManager Delegation ====================

    public int getTopLine() {
        return viewport.getTopLine();
    }

    public void setTopLine(int topLine) {
        viewport.setTopLine(topLine);
    }

    public int getCursorCol() {
        return viewport.getCursorCol();
    }

    public void setCursorCol(int cursorCol) {
        viewport.setCursorCol(cursorCol);
    }

    public int getCursorRow() {
        return viewport.getCursorRow();
    }

    public void setCursorRow(int cursorRow) {
        viewport.setCursorRow(cursorRow);
    }

    public int getCurrentLineSize() {
        return viewport.getCurrentLineSize();
    }

    public void moveCursorLeft(int amount) {
        viewport.moveCursorLeft(amount);
    }

    public void moveCursorRight(int amount) {
        viewport.moveCursorRight(amount);
    }

    public void moveCursorUp(int amount) {
        viewport.moveCursorUp(amount);
    }

    public void moveCursorDown(int amount, int maxVisibleLines) {
        viewport.moveCursorDown(amount, maxVisibleLines);
    }

    // ==================== TextOperations Delegation ====================

    public void appendChar(char character) {
        textOps.appendChar(character);
    }

    public void delChar() {
        textOps.delChar();
    }

    public void enter(int maxVisibleLines) {
        textOps.enter(maxVisibleLines);
    }

    public void deleteLine(int index) {
        textOps.deleteLine(index);
    }

    public void deleteLines(int start, int end) {
        textOps.deleteLines(start, end);
    }

    // ==================== TokenNavigator Delegation ====================

    public void setCursorToPreviousToken() {
        navigator.setCursorToPreviousToken();
    }

    public void setCursorToNextToken(int maxVisibleLines) {
        navigator.setCursorToNextToken(maxVisibleLines);
    }

    // ==================== Method Info Management ====================

    public void updateMethodInfos(boolean force) {
        if (!state.isEdited() && !force) {
            return;
        }
        String code = Utils.mergeLines(state.getLines());
        state.updateMethodInfos(code);
    }

    // ==================== Token Management ====================

    public void updateTokens(boolean force) {
        if (!state.isEdited() && !force) {
            return;
        }
        if (MainFrame.tech != Tech.REACT) {
            return;
        }
        String code = Utils.mergeLines(state.getLines());
        try {
            setLines(Utils.getReactLinesFromCode(code));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getSelectedString() {
      var selection =  this.textOps.getSelection();
      if(selection.fromLine() == selection.toLine()) {
        return  this.state.getLine(selection.fromLine()).text.substring(selection.fromColumn(), selection.toColumn());
      }

      // TODO other funktions
      return "";
    }

    public void selectCurrentLine() {
      var row =  this.viewport.getCursorRow();
      var line = this.state.getLine(row);
      if(line == null) {
          return;
      }
      var selection = new Selection(row, 0, row, -1, line.text.length());
      textOps.setSelection(selection);
    }

    public void setCursorToPrevMethodStart() {
        var selection =  this.viewport.setCursorToPrevMethodStart(this.state.getLines(), this.state.getMethodInfos());
        if(selection!=null){
            textOps.setSelection(selection);
        }
    }
    public void setCursorToNextMethodStart() {
      var selection =  this.viewport.setCursorToNextMethodStart(this.state.getLines(), this.state.getMethodInfos());
      if(selection!=null){
          textOps.setSelection(selection);
      }
    }

    public void setCursorPosition(CursorPosition cursorPosition) {
        this.viewport.setCursorCol(cursorPosition.col());
        this.viewport.setCursorRow(cursorPosition.line());
    }

    public CursorPosition getCursorPosition() {
        return new CursorPosition(this.viewport.getCursorRow(), this.viewport.getCursorCol());
    }
}