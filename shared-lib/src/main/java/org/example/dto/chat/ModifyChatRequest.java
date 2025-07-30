package org.example.dto.chat;

import jakarta.validation.constraints.NotEmpty;

public record ModifyChatRequest(
        @NotEmpty
        String name,
        @NotEmpty
        String imageUrl
) {
}
