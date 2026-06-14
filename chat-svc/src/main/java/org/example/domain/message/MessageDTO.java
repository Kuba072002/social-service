package org.example.domain.message;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.UUID;

public record MessageDTO(
        Long chatId,
        UUID messageId,
        Long senderId,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String content,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String mediaContent,
        Instant timestamp,
        String state
) {
}
