package org.example.application.chat.dto;

import java.time.Instant;

public record UpdateChatReadAtRequest(
        Long chatId,
        Instant lastReadAt
) {
}
