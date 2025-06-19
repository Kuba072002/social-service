package org.example.application.message.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.Instant;

@Builder
public record MessageEvent(
        String type,
        Long chatId,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Instant lastMessageCreatedAt,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Long userId,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Instant lastReadAt
) {
    public static MessageEvent post(Long chatId, Instant lastMessageCreatedAt) {
        return MessageEvent.builder()
                .type("POST")
                .chatId(chatId)
                .lastMessageCreatedAt(lastMessageCreatedAt)
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
