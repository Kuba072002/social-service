package org.example.application.chat.dto;

import jakarta.validation.constraints.NotEmpty;

public record ModifyChatRequest(
        @NotEmpty
        String name,
        @NotEmpty
        String imageUrl
) {
}
