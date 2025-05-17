package org.example.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record MessageRequest(
        @NotNull
        Long chatId,
        @NotEmpty
        String content
) {
}
