package org.example.application.chat;

import java.util.Set;

public record ChatEvent(
        String type,
        Long chatId,
        Set<Long> userIds
) {
}
