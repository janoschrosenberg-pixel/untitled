package blocks;

public record Block(
        char open,
        int startColumn,
        int endColumn,
        int startLine,
        int endLine
) {}