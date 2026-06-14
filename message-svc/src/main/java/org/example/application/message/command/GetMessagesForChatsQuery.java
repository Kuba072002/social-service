package org.example.application.message.command;

import java.util.Set;

public record GetMessagesForChatsQuery(
        Set<Long> chatIds,
        Integer limit
) {
}
