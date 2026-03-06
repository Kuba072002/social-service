package org.example.domain.message;

import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MessageFacade {
    private final MessageRepository messageRepository;

    @Transactional
    public void createMessage(Message message) {
        messageRepository.save(message);
    }

    @Transactional
    public void editMessage(Message message, String content) {
        message.setContent(content);
        message.setTimestamp(Instant.now());
        message.setState(MessageState.EDITED);
        messageRepository.save(message);
    }

    @Transactional
    public void deleteMessage(Message message) {
        message.setTimestamp(Instant.now());
        message.setState(MessageState.DELETED);
        messageRepository.save(message);
    }

    public List<Message> getMessages(Long chatId, Instant from, Instant to, Integer limit) {
        UUID fromUUID = UuidCreator.getTimeOrderedEpoch(from);
        UUID toUUID = UuidCreator.getTimeOrderedEpoch(to);
        return messageRepository.findAllByChatIdAndMessageIdBetween(chatId, fromUUID, toUUID, limit);
    }

    public Optional<Message> find(Long chatId, UUID messageId) {
        return messageRepository.findByChatIdAndMessageId(chatId, messageId);
    }

    public void delete(Long chatId) {
        messageRepository.deleteByChatId(chatId);
    }

}
