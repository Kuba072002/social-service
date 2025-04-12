package org.example.domain.chat.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.application.chat.ChatMapper;
import org.example.application.chat.dto.ChatParticipantsDTO;
import org.example.application.chat.dto.ChatsResponse;
import org.example.domain.chat.entity.Chat;
import org.example.domain.chat.entity.ChatParticipant;
import org.example.domain.chat.event.ChatEvent;
import org.example.domain.chat.repository.ChatParticipantRepository;
import org.example.domain.chat.repository.ChatRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.example.common.Constants.ADMIN_ROLE;
import static org.example.domain.chat.event.ChatEventType.ADD_PARTICIPANTS;
import static org.example.domain.chat.event.ChatEventType.CREATE;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMapper chatMapper;
    private final ChatEventPublisher chatEventPublisher;

    public boolean checkIfPrivateChatExists(Long user1Id, Long user2Id) {
        return chatRepository.existsPrivateChat(user1Id, user2Id);
    }

    @Transactional
    public Chat createChat(Long userId, Chat chat, Set<Long> userIds) {
        chatRepository.save(chat);

        var chatParticipants = userIds.stream()
                .map(id -> new ChatParticipant(chat, id))
                .collect(Collectors.toList());

        if (chat.getIsPrivate()){
            chatParticipants.getFirst().setRole(ADMIN_ROLE);
        }

        chatParticipants.add(new ChatParticipant(chat, userId, ADMIN_ROLE));
        chatParticipantRepository.saveAll(chatParticipants);

        userIds.add(userId);
        chatEventPublisher.sendEvent(new ChatEvent(CREATE.name(),chat.getId(),userIds));
        return chat;
    }

    @Transactional
    public void addToChat(Long userId, Chat chat, Set<Long> userIds) {
        var chatParticipants = userIds.stream()
                .map(id -> new ChatParticipant(chat, id))
                .toList();
        chatParticipantRepository.saveAll(chatParticipants);
        userIds.add(userId);
        chatEventPublisher.sendEvent(new ChatEvent(ADD_PARTICIPANTS.name(),chat.getId(),userIds));
    }

    public Optional<Chat> findChatWithParticipants(Long chatId) {
        return chatRepository.findChatWithParticipantsById(chatId);
    }

    public ChatParticipantsDTO findChatParticipants(Long chatId) {
        var chatParticipants = chatParticipantRepository.findAllByChatId(chatId);
        return chatMapper.toChatParticipantsDTO(chatParticipants);
    }

    public ChatsResponse getChats(Long userId) {
        List<Chat> chats = chatParticipantRepository.findAllChatsByUserId(userId);
        return chatMapper.toChatResponse(chats);
    }
}
