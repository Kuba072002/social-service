package org.example.application.dto;

import java.util.List;

public record LatestChatMessagesDTO(
        Long chatId,
        List<MessageDTO> messages) {
}
