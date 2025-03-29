package org.example.application.chat.dto;

import jakarta.validation.constraints.NotEmpty;

import java.time.Instant;
import java.util.Set;

public record ChatParticipantsDTO(
        @NotEmpty
        Set<Long> userIds
) {
}
