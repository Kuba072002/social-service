package org.example.application.chat.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

public record ParticipantDTO(
        Long userId,
        String userName,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String imageUrl,
        String role,
        Instant joinedAt,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Instant lastSeenAt
) {
}
