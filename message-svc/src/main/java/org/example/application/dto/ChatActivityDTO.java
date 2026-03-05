package org.example.application.dto;

import java.time.Instant;

public record ChatActivityDTO(
            Long chatId,
            Long userId,
            Instant lastReadAt
) {
}
