package org.example.dto.chat;

import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record UpdateChatReadAtRequest(
        @NotNull
        OffsetDateTime lastReadAt
) {
}
