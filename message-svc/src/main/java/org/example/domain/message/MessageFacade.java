package org.example.domain.message;

import lombok.RequiredArgsConstructor;
import org.example.application.MessageDTO;
import org.example.domain.chat.ChatFacade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MessageFacade {
    private final ChatFacade chatFacade;
    private final MessageMapper messageMapper;
    private final MessageRepository messageRepository;
    @Value("{message.query.default.limit}")
    private final Integer defaultLimit;

    public void createMessage(Long senderId, MessageDTO messageDTO) {
        validateRequester(messageDTO.chatId(), senderId);
        var message = messageMapper.toMessage(senderId, messageDTO);
        messageRepository.save(message);
    }

    public List<Message> getMessages(Long userId, Long chatId, Instant from, Instant to, Integer limit) {
        if (to == null) to = Instant.now();
        if (from == null) from = Instant.MIN;
        if (limit == null) limit = defaultLimit;
        validateQueryParams(from,to);
        validateRequester(chatId, userId);

        return messageRepository.findAllByChatIdAndCreatedAtGreaterThan(chatId, from, to, limit);
    }

    private void validateRequester(Long chatId, Long senderId) {
        var chatParticipants = chatFacade.getChat(chatId);
        chatParticipants.userIds().stream()
                .filter(p -> p.equals(senderId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Sender not involved in chat"));
    }

    private void validateQueryParams(Instant from, Instant to) {
        if (from.isAfter(to)) throw new RuntimeException("From cannot be greater than to.");
    }

}
