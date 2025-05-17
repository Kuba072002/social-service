package org.example.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

public record MessageDTO(
        Long chatId,
        Long senderId,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String content,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String mediaContent,
        Instant createdAt
) {
}
