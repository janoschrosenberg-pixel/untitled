package tokenizer;

public class TokenUtils {
    public static boolean isOnlyWhitespace(String s) {
        return s != null && s.chars().allMatch(Character::isWhitespace);
    }
}
