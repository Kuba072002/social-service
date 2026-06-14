package org.example.application.message.command;

import java.time.Instant;

public record GetMessagesQuery(
        Long userId,
        Long chatId,
        Instant before,
        Integer limit
) {
}
