package org.example.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MessageRequest(
        @NotNull
        Long chatId,
        @NotEmpty
        String content,
        @NotNull
        UUID clientMessageId
) {
}
