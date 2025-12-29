package tokenizer.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TokenType(
    String label,
    String keyword,
    Boolean beforeExpr,
    Boolean startsExpr,
    Boolean isAssign,
    Boolean prefix,
    Boolean postfix,
    Integer binop
) {}
