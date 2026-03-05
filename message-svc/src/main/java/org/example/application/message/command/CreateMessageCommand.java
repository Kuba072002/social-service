package org.example.application.message.command;

public record CreateMessageCommand(
        Long userId,
        Long chatId,
        String content
) {
}
