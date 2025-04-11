package org.example.application.chat.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record AddToChatRequest(
        @NotNull
        Long chatId,
        @NotEmpty
        Set<Long> userIds
) {
}
