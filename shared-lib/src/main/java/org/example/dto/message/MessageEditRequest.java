package org.example.dto.message;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MessageEditRequest(
        @NotNull
        Long chatId,
        @NotNull
        UUID messageId,
        @NotEmpty
        String content
) {
}
