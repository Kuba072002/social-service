package org.example.domain.message;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MessageFacade {
    private final MessageRepository messageRepository;

    @Transactional
    public void createMessage(Message message) {
        messageRepository.save(message);
    }

    public List<Message> getMessages(Long chatId, Instant from, Instant to, Integer limit) {
        return messageRepository.findAllByChatIdAndCreatedAtBetween(chatId, from, to, limit);
    }

    public void delete(Long chatId) {
        messageRepository.deleteByChatId(chatId);
    }

}
