package org.example.domain.chat;

import lombok.RequiredArgsConstructor;
import org.example.application.chat.ChatParticipantsDTO;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatFacade {
    private final ChatService chatService;

    @Cacheable(value = "chatParticipants", key = "#chatId")
    public ChatParticipantsDTO findChatParticipants(Long chatId) {
        return chatService.findChatParticipants(chatId);
    }

    @CacheEvict(value = "chatParticipants", key = "#chatId")
    public void evictChatParticipants(Long chatId) {
    }
}
