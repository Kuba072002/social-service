package org.example.application.message.command;

import java.util.UUID;

public record CreateMessageCommand(
        Long userId,
        Long chatId,
        String content,
        UUID clientMessageId
) {
}
