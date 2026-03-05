package org.example.application.message.command;

import java.util.UUID;

public record DeleteMessageCommand(
        Long userId,
        Long chatId,
        UUID messageId
) {
}
