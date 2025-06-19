package org.example.domain.chat;

import lombok.RequiredArgsConstructor;
import org.example.domain.chat.entity.Chat;
import org.example.domain.chat.entity.ChatDetail;
import org.example.domain.chat.entity.ChatParticipant;
import org.example.domain.chat.repository.ChatParticipantRepository;
import org.example.domain.chat.repository.ChatRepository;
import org.example.domain.event.ChatEvent;
import org.example.domain.event.ChatEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

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
    public void createChat(Chat chat) {
        chatRepository.save(chat);
        chatEventPublisher.sendEvent(ChatEvent.create(chat.getId(), chat.getParticipants()));
    }

    @Transactional
    public void modifyParticipants(
            Chat chat,
            List<ChatParticipant> newParticipants,
            List<ChatParticipant> participantsToDelete
    ) {
        chat.getParticipants().removeAll(participantsToDelete);
        chat.getParticipants().addAll(newParticipants);
        chatParticipantRepository.saveAll(newParticipants);
        chatParticipantRepository.deleteAll(participantsToDelete);
        chatEventPublisher.sendEvent(ChatEvent.modify(chat.getId(), chat.getParticipants()));
    }

    public Optional<Chat> findChatWithParticipants(Long chatId) {
        return chatRepository.findWithParticipantsById(chatId);
    }

    public List<ChatParticipant> findChatParticipants(Long chatId) {
        return chatParticipantRepository.findByChatId(chatId);
    }

    public List<ChatDetail> findUserGroupChatDetails(Long userId, Integer pageNumber, Integer pageSize) {
        int offset = (pageNumber - 1) * pageSize;
        return chatParticipantRepository.findUserGroupChats(userId, offset, pageSize);
    }

    public List<ChatDetail> findUserPrivateChatDetails(Long userId, Integer pageNumber, Integer pageSize) {
        int offset = (pageNumber - 1) * pageSize;
        return chatParticipantRepository.findUserPrivateChats(userId, offset, pageSize);
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

    public void save(Chat chat) {
        chatRepository.save(chat);
    }

    public void save(ChatParticipant chatParticipant) {
        chatParticipantRepository.save(chatParticipant);
    }

    @Transactional
    public void delete(Long chatId) {
        chatRepository.deleteById(chatId);
        chatEventPublisher.sendEvent(ChatEvent.delete(chatId));
    }

    @Transactional
    public void deleteParticipant(Chat chat, ChatParticipant chatParticipant) {
        chat.getParticipants().remove(chatParticipant);
        chatParticipantRepository.delete(chatParticipant);
        chatEventPublisher.sendEvent(ChatEvent.modify(chat.getId(), chat.getParticipants()));
    }
}
