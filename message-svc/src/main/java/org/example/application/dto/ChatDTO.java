package org.example.application.dto;

import java.time.Instant;
import java.util.Set;

public record ChatDTO(
        Long id,
        String name,
        String imageUrl,
        boolean isPrivate,
        Instant createdAt,
        Set<Long> participantIds
) {
}
