package org.example.domain.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class ChatFacade {
    private final ChatService chatService;

    @Cacheable(value = "chatParticipantIds", key = "#chatId")
    public Set<Long> findChatParticipants(Long chatId) {
        return chatService.findChatParticipantIds(chatId);
    }

    @CacheEvict(value = "chatParticipantIds", key = "#chatId")
    public void evictChatParticipants(Long chatId) {
    }
}
