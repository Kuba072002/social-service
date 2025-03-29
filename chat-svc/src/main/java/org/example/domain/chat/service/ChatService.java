package org.example.domain.chat.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.application.chat.ChatMapper;
import org.example.application.chat.dto.ChatDTO;
import org.example.domain.chat.entity.Chat;
import org.example.domain.chat.entity.ChatParticipant;
import org.example.domain.chat.repository.ChatParticipantRepository;
import org.example.domain.chat.repository.ChatRepository;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMapper chatMapper;

    public boolean checkIfPrivateChatExists(Long user1Id, Long user2Id) {
        return chatRepository.existsPrivateChat(user1Id, user2Id);
    }

    @Transactional
    public Chat createChat(Long userId, Chat chat, Set<Long> userIds) {
        chatRepository.save(chat);

        var chatParticipants = userIds.stream()
                .map(id -> new ChatParticipant(chat, id))
                .collect(Collectors.toList());

        chatParticipants.add( new ChatParticipant(chat, userId));
        chatParticipantRepository.saveAll(chatParticipants);
        return chat;
    }

    public ChatDTO getChat(Long chatId) {
        return chatRepository.findChat(chatId)
                .map(chatMapper::toChatDTO)
                .orElseThrow(() -> new RuntimeException("Chat not exists."));
    }
}
