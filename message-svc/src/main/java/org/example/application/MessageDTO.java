package org.example.application;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record MessageDTO(
        @NotNull
        Long chatId,
        @NotEmpty
        String content
) {
}
