package org.example.domain.event;

import org.example.domain.chat.entity.ChatParticipant;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record ChatEvent(
        String type,
        Long chatId,
        Set<Long> userIds
) {

    public static ChatEvent create(Long chatId, List<ChatParticipant> participants) {
        return new ChatEvent(
                ChatEventType.CREATE.name(),
                chatId,
                participants.stream()
                        .map(ChatParticipant::getUserId)
                        .collect(Collectors.toSet())
        );
    }

    public static ChatEvent modify(Long chatId, List<ChatParticipant> participants) {
        return new ChatEvent(
                ChatEventType.MODIFY.name(),
                chatId,
                participants.stream()
                        .map(ChatParticipant::getUserId)
                        .collect(Collectors.toSet())
        );
    }

    public static ChatEvent delete(Long chatId) {
        return new ChatEvent(
                ChatEventType.DELETE.name(),
                chatId,
                Collections.emptySet()
        );
    }
}
