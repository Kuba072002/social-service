package org.example.domain.message;

import java.time.Instant;

public record MessageEvent(
        Long chatId,
        Instant messageCreatedAt
) {
}
