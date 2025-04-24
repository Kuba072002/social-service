package org.example.application.chat.dto;

import java.time.Instant;

public record ParticipantDTO(
        Long userId,
        String userName,
        String imageUrl,
        String role,
        Instant joinedAt
) {
}
