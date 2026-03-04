package org.example.application.dto;

import java.time.Instant;

public record UserDTO(
        Long id,
        String userName,
        String email,
        String imageUrl,
        Instant lastSeenAt
) {
}
