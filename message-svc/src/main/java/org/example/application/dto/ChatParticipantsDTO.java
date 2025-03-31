package org.example.application.dto;

import java.io.Serializable;
import java.util.Set;

public record ChatParticipantsDTO(
        Set<Long> userIds
) implements Serializable {
}
