package org.example.application.message.service;

import lombok.RequiredArgsConstructor;
import org.example.ApplicationException;
import org.example.application.dto.MessageDTO;
import org.example.application.chat.ChatAccessValidator;
import org.example.domain.message.MessageFacade;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.example.common.MessageApplicationError.FROM_GREATER_THAN_TO;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageFacade messageFacade;
    private final MessageMapper messageMapper;
    private final ChatAccessValidator chatAccessValidator;

    public List<MessageDTO> getMessages(Long userId, Long chatId, Instant from, Instant to, Integer limit) {
        if (to == null) to = Instant.now();
        if (from == null) from = Instant.now().minus(Duration.ofDays(365));
        validateQueryParams(from, to);
        chatAccessValidator.getParticipantsIfAllowed(chatId, userId);

        var messages = messageFacade.getMessages(chatId, from, to, limit);
        return messages.stream()
                .map(messageMapper::toMessageDTO)
                .toList();
    }

    private void validateQueryParams(Instant from, Instant to) {
        if (from.isAfter(to)) throw new ApplicationException(FROM_GREATER_THAN_TO);
    }
}
