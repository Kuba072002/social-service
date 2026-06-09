package org.example.application.message.command;

import java.time.Instant;

public record GetMessagesCommand(
        Long userId,
        Long chatId,
        Instant before,
        Integer limit
) {
}
