package org.example.application.chat.dto;

import java.time.Instant;

public record ChatDTO(
        Long id,
        String name,
        String imageUrl,
        boolean isPrivate,
        Instant createdAt
) {
}
