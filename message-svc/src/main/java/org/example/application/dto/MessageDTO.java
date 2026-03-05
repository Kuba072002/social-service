package org.example.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.example.domain.message.MessageState;

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
        MessageState state
) {
}
