package org.example.domain.chat;

import lombok.RequiredArgsConstructor;
import org.example.application.dto.ChatDTO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatFacade {
    private final ChatService chatService;

    @Cacheable("chats")
    public ChatDTO getChat(Long chatId) {
        return chatService.getChat(chatId);
    }
}
