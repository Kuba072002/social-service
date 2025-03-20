package org.example.domain.message;

import com.fasterxml.jackson.annotation.JsonInclude;

public record MessageContent(
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String content
) {
}
