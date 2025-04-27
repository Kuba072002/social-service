package org.example.application.message;

import java.time.Instant;

public record MessageEvent(
        Long chatId,
        Instant messageCreatedAt
) {
}
