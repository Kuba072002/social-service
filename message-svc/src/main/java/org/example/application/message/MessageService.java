package org.example.application.message;

import lombok.RequiredArgsConstructor;
import org.example.ApplicationException;
import org.example.application.dto.MessageDTO;
import org.example.application.dto.MessageEditRequest;
import org.example.application.dto.MessageRequest;
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
import java.util.UUID;

import static org.example.common.MessageApplicationError.*;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageFacade messageFacade;
    private final ChatFacade chatFacade;
    private final MessageMapper messageMapper;
    private final MessagePublisher messagePublisher;

    @Value("${message.query.default.limit}")
    private Integer defaultLimit;

    public void createMessage(Long senderId, MessageRequest messageRequest) {
        var chatParticipantIds = findChatParticipantIds(messageRequest.chatId());
        validateRequester(chatParticipantIds, senderId);

        var message = messageMapper.toMessage(senderId, messageRequest);
        messageFacade.saveMessage(message);
        var event = MessageEvent.post(message.getChatId(), message.getCreatedAt(), chatParticipantIds);
        messagePublisher.publish(event);
    }

    public void editMessage(Long senderId, MessageEditRequest messageEditRequest) {
        var message = findMessageAndValidateSender(
                senderId, messageEditRequest.chatId(), messageEditRequest.messageId());
        message.setContent(messageEditRequest.content());
        messageFacade.saveMessage(message);
    }

    public void deleteMessage(Long senderId, Long chatId, UUID messageId) {
        var message = findMessageAndValidateSender(senderId, chatId, messageId);
        message.setContent("");
        messageFacade.saveMessage(message);
    }

    public List<MessageDTO> getMessages(Long userId, Long chatId, Instant from, Instant to, Integer limit) {
        if (to == null) to = Instant.now();
        if (from == null) from = Instant.now().minus(Duration.ofDays(365));
        if (limit == null) limit = defaultLimit;
        validateQueryParams(from, to);
        validateRequester(findChatParticipantIds(chatId), userId);

        var messages = messageFacade.getMessages(chatId, from, to, limit);
        if (!messages.isEmpty()) {
            var event = MessageEvent.get(chatId, userId, messages.getFirst().getCreatedAt());
            messagePublisher.publish(event);
        }
        return messages.stream()
                .map(messageMapper::toMessageDTO)
                .toList();
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

    private Message findMessageAndValidateSender(Long senderId, Long chatId, UUID messageId) {
        var message = messageFacade.find(chatId, messageId)
                .orElseThrow(() -> new ApplicationException(MESSAGE_NOT_FOUND));
        if (!message.getSenderId().equals(senderId)) {
            throw new ApplicationException(SENDER_MISMATCH);
        }
        return message;
    }
}
