package org.example.domain.chat.event;

import java.util.Set;

public record ChatEvent(
        String type,
        Long chatId,
        Set<Long> userIds
) {
}
