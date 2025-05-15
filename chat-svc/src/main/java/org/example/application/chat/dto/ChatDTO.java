package org.example.application.chat.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

public record ChatDTO(
        Long id,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String name,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String imageUrl,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Long userId,
        Boolean isPrivate,
        Instant lastReadAt,
        Instant lastMessageAt
) {
}
