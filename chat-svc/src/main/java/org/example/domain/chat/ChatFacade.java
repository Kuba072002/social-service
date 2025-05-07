package org.example.domain.chat;

import lombok.RequiredArgsConstructor;
import org.example.domain.chat.entity.Chat;
import org.example.domain.chat.entity.ChatParticipant;
import org.example.domain.chat.repository.ChatParticipantRepository;
import org.example.domain.chat.repository.ChatRepository;
import org.example.domain.event.ChatEvent;
import org.example.domain.event.ChatEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.example.common.Constants.ADMIN_ROLE;

@Service
@RequiredArgsConstructor
public class ChatFacade {
    private final ChatRepository chatRepository;
    private final ChatParticipantRepository chatParticipantRepository;

    private final ChatEventPublisher chatEventPublisher;

    public boolean checkIfPrivateChatExists(Long user1Id, Long user2Id) {
        return chatRepository.existsPrivateChat(user1Id, user2Id);
    }

    @Transactional
    public void createChat(Long userId, Chat chat, Set<Long> userIds) {
        var chatParticipants = userIds.stream()
                .map(id -> new ChatParticipant(chat, id))
                .collect(Collectors.toList());
        if (chat.getIsPrivate()) {
            chatParticipants.getFirst().setRole(ADMIN_ROLE);
        }
        chatParticipants.add(new ChatParticipant(chat, userId, ADMIN_ROLE));
        chat.setParticipants(chatParticipants);

        chatRepository.save(chat);
        chatEventPublisher.sendEvent(ChatEvent.createEvent(chat));
    }

    @Transactional
    public void addToChat(Chat chat, Set<Long> userIds) {
        var chatParticipants = userIds.stream()
                .map(id -> new ChatParticipant(chat, id))
                .toList();
        chatParticipantRepository.saveAll(chatParticipants);
        chatEventPublisher.sendEvent(ChatEvent.addParticipantsEvent(chat.getId(), chatParticipants));
    }

    public Optional<Chat> findChatWithParticipants(Long chatId) {
        return chatRepository.findById(chatId);
    }

    public List<ChatParticipant> findChatParticipants(Long chatId) {
        return chatParticipantRepository.findByChat_Id(chatId);
    }

    public List<ChatParticipant> getChats(Long userId, Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(
                pageNumber != null ? pageNumber : 0,
                pageSize != null ? pageSize : 20,
                Sort.by(Sort.Direction.DESC, "chat.lastMessageAt")
        );
        return chatParticipantRepository.findByUserId(userId, pageRequest);
    }

    @Transactional
    public void updateLastMessageAt(Long chatId, Instant lastMessageAt) {
        chatRepository.updateLastMessageAt(chatId, lastMessageAt);
    }

    @Transactional
    public void updateLastReadAt(Long chatId, Long userId, Instant lastReadAt) {
        chatParticipantRepository.updateLastReadAt(chatId, userId, lastReadAt);
    }

    public Optional<ChatParticipant> getChatParticipant(Long chatId, Long userId) {
        return chatParticipantRepository.findByChatIdAndUserId(chatId, userId);
    }

    public void save(ChatParticipant chatParticipant) {
        chatParticipantRepository.save(chatParticipant);
    }
}
