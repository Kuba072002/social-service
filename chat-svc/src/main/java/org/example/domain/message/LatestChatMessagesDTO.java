package org.example.domain.message;

import java.util.List;

public record LatestChatMessagesDTO(
        Long chatId,
        List<MessageDTO> messages
) {
}
