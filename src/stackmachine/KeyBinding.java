package stackmachine;

public record KeyBinding(char key, String method) {
    @Override
    public String toString() {
        return key+":"+method;
    }

    public static KeyBinding parse(String string) {
        var parts = string.split(":");
        return new KeyBinding(parts[0].charAt(0), parts[1]);
    }
}
