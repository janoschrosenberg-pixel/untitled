package stackmachine;

public record KeyBinding(String key,String mode, String method) {
    @Override
    public String toString() {
        return key+":"+mode+":"+method;
    }

    public static KeyBinding parse(String string) {
        var parts = string.split(":");
        if(parts.length == 2) {
            return new KeyBinding(parts[0], "normal", parts[1]);
        }
        return new KeyBinding(parts[0], parts[1], parts[2]);
    }
}
