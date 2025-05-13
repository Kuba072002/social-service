package org.example.application.message;

import lombok.RequiredArgsConstructor;
import org.example.ApplicationException;
import org.example.application.dto.MessageDTO;
import org.example.application.message.event.MessageEvent;
import org.example.application.message.event.MessagePublisher;
import org.example.domain.chat.ChatFacade;
import org.example.domain.message.Message;
import org.example.domain.message.MessageFacade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.example.common.MessageApplicationError.FROM_GREATER_THAN_TO;
import static org.example.common.MessageApplicationError.NOT_INVOLVED_REQUESTER;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageFacade messageFacade;
    private final ChatFacade chatFacade;
    private final MessageMapper messageMapper;
    private final MessagePublisher messagePublisher;

    @Value("${message.query.default.limit}")
    private Integer defaultLimit;

    public void createMessage(Long senderId, MessageDTO messageDTO) {
        var chatParticipantIds = findChatParticipantIds(messageDTO.chatId());
        validateRequester(chatParticipantIds, senderId);

        var message = messageMapper.toMessage(senderId, messageDTO);
        messageFacade.createMessage(message);
        messagePublisher.broadcastMessageAndPublishEvent(chatParticipantIds, message);
    }

    public List<Message> getMessages(Long userId, Long chatId, Instant from, Instant to, Integer limit) {
        if (to == null) to = Instant.now();
        if (from == null) from = Instant.now().minus(Duration.ofDays(365));
        if (limit == null) limit = defaultLimit;
        validateQueryParams(from, to);
        validateRequester(findChatParticipantIds(chatId), userId);

        var messages = messageFacade.getMessages(chatId, from, to, limit);
        if (!messages.isEmpty()) {
            var event = MessageEvent.createGetMessageEvent(chatId, userId, messages.getFirst().getCreatedAt());
            messagePublisher.publish(event);
        }
        return messages;
    }

    private void validateRequester(Set<Long> chatParticipantIds, Long requesterId) {
        chatParticipantIds.stream()
                .filter(participant -> participant.equals(requesterId))
                .findFirst()
                .orElseThrow(() -> new ApplicationException(NOT_INVOLVED_REQUESTER));
    }

    private Set<Long> findChatParticipantIds(Long chatId) {
        return chatFacade.findChatParticipants(chatId);
    }

    private void validateQueryParams(Instant from, Instant to) {
        if (from.isAfter(to)) throw new ApplicationException(FROM_GREATER_THAN_TO);
    }
}
