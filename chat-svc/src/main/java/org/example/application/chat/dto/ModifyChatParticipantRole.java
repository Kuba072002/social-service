package org.example.application.chat.dto;

import jakarta.validation.constraints.NotNull;

public record ModifyChatParticipantRole(
        @NotNull
        Long userId,
        @NotNull
        ChatParticipantRoleDTO role
) {
}
