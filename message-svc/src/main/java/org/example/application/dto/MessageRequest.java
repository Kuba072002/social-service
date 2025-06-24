package org.example.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MessageRequest(
        @NotNull
        Long chatId,
        @NotEmpty
        @Size(max = 2000)
        String content
) {
}
