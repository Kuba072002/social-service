package org.example.application.dto;

import java.util.Set;

public record ChatParticipantsDTO(
        Set<Long> userIds
) {
}
