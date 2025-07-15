package org.example.domain.event;

import org.example.domain.chat.entity.ChatParticipant;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record ChatEvent(
        ChatEventType type,
        Long chatId,
        Set<Long> userIds
) {

    public static ChatEvent create(Long chatId, List<ChatParticipant> participants) {
        return new ChatEvent(ChatEventType.CREATE, chatId, getUserIds(participants));
    }

    public static ChatEvent modify(Long chatId, List<ChatParticipant> participants) {
        return new ChatEvent(ChatEventType.MODIFY, chatId, getUserIds(participants));
    }

    public static ChatEvent delete(Long chatId) {
        return new ChatEvent(ChatEventType.DELETE, chatId, Collections.emptySet());
    }

    private static Set<Long> getUserIds(List<ChatParticipant> participants) {
        return participants.stream()
                .map(ChatParticipant::getUserId)
                .collect(Collectors.toSet());
    }
}
