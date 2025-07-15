package org.example.application.chat.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record UpdateChatReadAtRequest(
        @NotNull
        Instant lastReadAt
) {
}
