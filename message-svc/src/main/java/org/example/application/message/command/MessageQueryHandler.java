package org.example.application.message.command;

import lombok.RequiredArgsConstructor;
import org.example.application.chat.ChatAccessValidator;
import org.example.application.dto.LatestChatMessagesDTO;
import org.example.application.dto.MessageDTO;
import org.example.domain.message.MessageFacade;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageQueryHandler {
    private final MessageFacade messageFacade;
    private final ChatAccessValidator chatAccessValidator;

    public List<MessageDTO> handle(GetMessagesQuery command) {
        chatAccessValidator.validateParticipant(command.chatId(), command.userId());
        return messageFacade.getMessages(command.chatId(), command.before(), command.limit());
    }

    public List<LatestChatMessagesDTO> handle(GetMessagesForChatsQuery command) {
        Instant now = Instant.now();
        return command.chatIds().stream()
                .map(chatId -> {
                    var messages = messageFacade.getMessages(chatId, now, command.limit());
                    return new LatestChatMessagesDTO(chatId, messages);
                })
                .toList();
    }
}
