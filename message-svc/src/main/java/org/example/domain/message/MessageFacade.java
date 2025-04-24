package org.example.domain.message;

import lombok.RequiredArgsConstructor;
import org.example.ApplicationException;
import org.example.application.message.MessageDTO;
import org.example.domain.chat.ChatFacade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.example.common.MessageApplicationError.FROM_GREATER_THAN_TO;
import static org.example.common.MessageApplicationError.NOT_INVOLVED_REQUESTER;

@Component
@RequiredArgsConstructor
public class MessageFacade {
    private final ChatFacade chatFacade;
    private final MessageMapper messageMapper;
    private final MessageRepository messageRepository;
    @Value("${message.query.default.limit}")
    private Integer defaultLimit;

    public void createMessage(Long senderId, MessageDTO messageDTO) {
        validateRequester(messageDTO.chatId(), senderId);
        var message = messageMapper.toMessage(senderId, messageDTO);
        messageRepository.save(message);
    }

    public List<Message> getMessages(Long userId, Long chatId, Instant from, Instant to, Integer limit) {
        if (to == null) to = Instant.now();
        if (from == null) from = Instant.now().minus(Duration.ofDays(365));
        if (limit == null) limit = defaultLimit;
        validateQueryParams(from,to);
        validateRequester(chatId, userId);

        return messageRepository.findAllByChatIdAndCreatedAtBetween(chatId, from, to, limit);
    }

    private void validateRequester(Long chatId, Long requesterId) {
        var chatParticipantIds = chatFacade.findChatParticipants(chatId);
        chatParticipantIds.stream()
                .filter(participant -> participant.equals(requesterId))
                .findFirst()
                .orElseThrow(() -> new ApplicationException(NOT_INVOLVED_REQUESTER));
    }

    private void validateQueryParams(Instant from, Instant to) {
        if (from.isAfter(to)) throw new ApplicationException(FROM_GREATER_THAN_TO);
    }

}
