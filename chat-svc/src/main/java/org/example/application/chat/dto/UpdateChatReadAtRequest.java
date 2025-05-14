package org.example.application.chat.dto;

import java.time.Instant;

public record UpdateChatReadAtRequest(
        Instant lastReadAt
) {
}
