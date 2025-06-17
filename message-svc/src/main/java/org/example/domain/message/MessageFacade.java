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
    public void saveMessage(Message message) {
        messageRepository.save(message);
    }

    public List<Message> getMessages(Long chatId, Instant from, Instant to, Integer limit) {
        UUID fromUuid = UuidCreator.getTimeOrderedEpoch(from);
        UUID toUuid = UuidCreator.getTimeOrderedEpoch(to);
        return messageRepository.findAllByChatIdAndMessageIdBetween(chatId, fromUuid, toUuid, limit);
    }

    public Optional<Message> find(Long chatId, UUID messageId) {
        return messageRepository.findByChatIdAndMessageId(chatId, messageId);
    }

    public void delete(Long chatId) {
        messageRepository.deleteByChatId(chatId);
    }

}
