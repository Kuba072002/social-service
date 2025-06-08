package org.example.domain.chat;

import lombok.RequiredArgsConstructor;
import org.example.domain.chat.entity.Chat;
import org.example.domain.chat.entity.ChatParticipant;
import org.example.domain.chat.repository.ChatParticipantRepository;
import org.example.domain.chat.repository.ChatRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatFacade {
    private final ChatRepository chatRepository;
    private final ChatParticipantRepository chatParticipantRepository;

    public boolean checkIfPrivateChatExists(Long user1Id, Long user2Id) {
        return chatRepository.existsPrivateChat(user1Id, user2Id);
    }

    public void createChat(Chat chat) {
        chatRepository.save(chat);
    }

    public void saveParticipants(List<ChatParticipant> chatParticipants) {
        chatParticipantRepository.saveAll(chatParticipants);
    }

    public Optional<Chat> findChatWithParticipants(Long chatId) {
        return chatRepository.findWithParticipantsById(chatId);
    }

    public List<ChatParticipant> findChatParticipants(Long chatId) {
        return chatParticipantRepository.findByChatId(chatId);
    }

    public List<ChatParticipant> findUserChatParticipants(Long userId, Boolean isPrivate, Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(
                pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "chat.lastMessageAt")
        );
        return chatParticipantRepository.findByUserIdAndChat_IsPrivate(userId, isPrivate, pageRequest);
    }

    public List<ChatParticipant> findParticipants(Collection<Long> chatIds) {
        return chatParticipantRepository.findAllByChatIdIn(chatIds);
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

    public void delete(Long chatId) {
        chatRepository.deleteById(chatId);
    }

    public void delete(ChatParticipant chatParticipant) {
        chatParticipantRepository.delete(chatParticipant);
    }
}
