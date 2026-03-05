package org.example.application.message.service;

import lombok.RequiredArgsConstructor;
import org.example.ApplicationException;
import org.example.application.dto.WsEvent;
import org.example.application.event.MessageEvent;
import org.example.application.event.OutboundMessagingService;
import org.example.application.message.command.CreateMessageCommand;
import org.example.application.message.command.DeleteMessageCommand;
import org.example.application.message.command.EditMessageCommand;
import org.example.application.message.validation.ChatAccessValidator;
import org.example.domain.message.Message;
import org.example.domain.message.MessageFacade;
import org.example.domain.message.MessageState;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static org.example.common.MessageApplicationError.MESSAGE_NOT_FOUND;
import static org.example.common.MessageApplicationError.SENDER_MISMATCH;

@Service
@RequiredArgsConstructor
public class MessageCommandHandler {
    private final MessageFacade messageFacade;
    private final ChatAccessValidator chatAccessValidator;
    private final MessageMapper messageMapper;
    private final OutboundMessagingService outboundMessagingService;

    public UUID handle(CreateMessageCommand command) {
        var chatParticipantIds = chatAccessValidator.validateRequesterAndReturnParticipants(command.chatId(), command.userId());

        var message = messageMapper.toMessage(command.userId(), command.chatId(), command.content());
        messageFacade.saveMessage(message);

        notifyParticipants(chatParticipantIds, message);
        return message.getMessageId();
    }

    public void handle(EditMessageCommand command) {
        var message = findMessageAndValidateSender(command.userId(), command.chatId(), command.messageId());
        var chatParticipantIds = chatAccessValidator.validateRequesterAndReturnParticipants(command.chatId(), command.userId());

        message.setContent(command.content());
        message.setTimestamp(Instant.now());
        message.setState(MessageState.EDITED);
        messageFacade.saveMessage(message);

        notifyParticipants(chatParticipantIds, message);
    }

    public void handle(DeleteMessageCommand command) {
        var message = findMessageAndValidateSender(command.userId(), command.chatId(), command.messageId());
        var chatParticipantIds = chatAccessValidator.validateRequesterAndReturnParticipants(command.chatId(), command.userId());

        message.setTimestamp(Instant.now());
        message.setState(MessageState.DELETED);
        messageFacade.saveMessage(message);

        notifyParticipants(chatParticipantIds, message);
    }

    private Message findMessageAndValidateSender(Long senderId, Long chatId, UUID messageId) {
        var message = messageFacade.find(chatId, messageId)
                .orElseThrow(() -> new ApplicationException(MESSAGE_NOT_FOUND));
        if (!Objects.equals(message.getSenderId(), senderId)) {
            throw new ApplicationException(SENDER_MISMATCH);
        }
        return message;
    }

    private void notifyParticipants(Set<Long> userIds, Message message) {
        outboundMessagingService.broadcastMessageAndPublishEventAsync(
                userIds,
                message.getSenderId(),
                WsEvent.of(message),
                MessageEvent.post(message.getChatId(), message.getTimestamp())
        );
    }
}
