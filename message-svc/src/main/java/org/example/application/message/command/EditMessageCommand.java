package org.example.application.message.command;

import java.util.UUID;

public record EditMessageCommand(
        Long userId,
        Long chatId,
        UUID messageId,
        String content
) {
}
