package editor;

public enum TempBuffer {
    INSTANCE;

    private StringBuilder sb = new StringBuilder();

    public void clear() {
        sb = new StringBuilder();
    }

    public String text(){
        return sb.toString();
    }

    public void add(char c) {
        sb.append(c);
    }

    public void del(){
        sb.deleteCharAt(sb.length()-1);
    }

}
