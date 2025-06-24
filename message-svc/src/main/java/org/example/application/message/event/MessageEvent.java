package org.example.application.message.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.Instant;
import java.util.Set;

@Builder
public record MessageEvent(
        String type,
        Long chatId,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Instant lastMessageCreatedAt,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Long userId,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Instant lastReadAt,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Set<Long> userIds
) {
    public static MessageEvent post(Long chatId, Instant lastMessageCreatedAt, Set<Long> userIds) {
        return MessageEvent.builder()
                .type("POST")
                .chatId(chatId)
                .lastMessageCreatedAt(lastMessageCreatedAt)
                .userIds(userIds)
                .build();
    }

    public static MessageEvent get(Long chatId, Long userId, Instant lastReadAt) {
        return MessageEvent.builder()
                .type("GET")
                .chatId(chatId)
                .userId(userId)
                .lastReadAt(lastReadAt)
                .build();
    }
}
