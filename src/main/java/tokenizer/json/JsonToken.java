package tokenizer.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JsonToken(
    TokenType type,
    Object value,     // String | Number | null
    int start,
    int end,
    Loc loc
) {}
