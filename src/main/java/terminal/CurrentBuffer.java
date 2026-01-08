package terminal;

import editor.EditorCommands;

public enum CurrentBuffer implements EditorCommands {
    CURRENT_BUFFER;

    private Buffer curentBuffer = new Buffer("untitled");

    public Runnable getEscapeAction() {
        return escapeAction;
    }

    public void setEscapeAction(Runnable escapeAction) {
        this.escapeAction = escapeAction;
    }

    public Runnable getStrgAction() {
        return strgAction;
    }

    public void setStrgAction(Runnable strgAction) {
        this.strgAction = strgAction;
    }

    private Runnable escapeAction;

    private Runnable strgAction;

    public int getMaxLines() {
        return maxLines;
    }

    public void setMaxLines(int maxLines) {
        this.maxLines = maxLines;
    }

    private int maxLines;
    public void setBuffer(Buffer buffer) {
        this.curentBuffer = buffer;
    }

    public Buffer getCurentBuffer() {
        return curentBuffer;
    }

    public void moveCursorDown(int amount){
        curentBuffer.moveCursorDown(amount,maxLines );
    }

    public void moveCursorLeft(int amount){
        curentBuffer.moveCursorLeft(amount);
    }

    public void  moveCursorRight(int amount){
        curentBuffer.moveCursorRight(amount);
    }

    public void moveCursorUp(int amount) {
        curentBuffer.moveCursorUp(amount);
    }

    @Override
    public void appendChar(char sign) {
        curentBuffer.appendChar(sign);
    }

    @Override
    public void delChar() {
        curentBuffer.delChar();
    }

    @Override
    public void enter() {
        curentBuffer.enter(maxLines);
    }

    @Override
    public void ctrlPressed() {
        curentBuffer.updateMethodInfos(false);
        curentBuffer.updateTokens(false);
        this.strgAction.run();
    }

    @Override
    public void ctrlReleased() {

    }

    @Override
    public String showModus() {
        return "EDITOR";
    }

    @Override
    public void esc() {
        escapeAction.run();
    }

    public void setCursorToNextToken() {
        this.curentBuffer.setCursorToNextToken(this.maxLines);
    }
    public void setCursorToPrevToken() {
        this.curentBuffer.setCursorToPreviousToken();
    }

    public void nextMethod() {
        this.curentBuffer.setCursorToNextMethodStart();
    }


    public void prevMethod() {
        this.curentBuffer.setCursorToPrevMethodStart();
    }
}
