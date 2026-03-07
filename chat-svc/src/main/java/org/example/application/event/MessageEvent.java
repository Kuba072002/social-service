package org.example.application.event;

import java.time.Instant;

public record MessageEvent(
        String type,
        Long chatId,
        Instant lastMessageCreatedAt,
        Long userId,
        Instant lastReadAt
) {
}
