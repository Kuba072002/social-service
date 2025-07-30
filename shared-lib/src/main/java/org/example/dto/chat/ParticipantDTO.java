package org.example.dto.chat;

import java.time.OffsetDateTime;

public record ParticipantDTO(
        Long userId,
        String userName,
        String imageUrl,
        String role,
        OffsetDateTime joinedAt
) {
}
