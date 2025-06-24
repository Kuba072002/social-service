package org.example.domain.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class ChatFacade {
    private final ChatService chatService;
    private final ChatRepository chatRepository;

    public Set<Long> findChatParticipants(Long chatId) {
        return chatRepository.findById(chatId)
                .map(chat -> chat.getUsers().getIds())
                .orElseGet(() -> chatService.findChatParticipantIds(chatId));
    }

    public void save(Long chatId, Set<Long> userIds) {
        var chat = new Chat(chatId, new Users(userIds));
        chatRepository.save(chat);
    }

    public void delete(Long chatId) {
        chatRepository.deleteById(chatId);
    }
}
