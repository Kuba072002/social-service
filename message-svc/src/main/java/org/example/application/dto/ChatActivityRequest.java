package org.example.application.dto;

import java.time.Instant;

public record ChatActivityRequest(
        Long chatId,
        Instant lastReadAt
) {
}

