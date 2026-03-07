package org.example.domain.chat;

import lombok.RequiredArgsConstructor;
import org.example.application.chat.ChatEvent;
import org.example.domain.message.MessageFacade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class ChatFacade {
    private final MessageFacade messageFacade;
    private final ChatRepository chatRepository;
    private final ChatService chatService;

    @Value("${feature.chat-local-read:true}")
    private boolean chatLocalReadEnabled;

    @Cacheable(value = "chatParticipantIds", key = "#chatId")
    public Set<Long> findChatParticipants(Long chatId) {
        if (chatLocalReadEnabled) {
            return chatRepository.findById(chatId)
                    .map(Chat::getParticipantIds)
                    .orElse(Set.of());
        } else {
            return chatService.findChatParticipantIds(chatId);
        }
    }

    @CacheEvict(value = "chatParticipantIds", key = "#event.chatId()")
    public void createOrModify(ChatEvent event, long timestamp) {
        chatRepository.upsert(
                event.chatId(),
                event.userIds(),
                event.updatedAt(),
                timestamp
        );
    }

    @CacheEvict(value = "chatParticipantIds", key = "#chatId")
    public void delete(Long chatId, Long timestamp) {
        chatRepository.delete(chatId, timestamp);
        messageFacade.delete(chatId);
    }
}
