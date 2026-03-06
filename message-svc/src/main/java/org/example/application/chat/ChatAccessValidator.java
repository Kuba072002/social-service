package org.example.application.chat;

import lombok.RequiredArgsConstructor;
import org.example.ApplicationException;
import org.example.domain.chat.ChatFacade;
import org.springframework.stereotype.Component;

import java.util.Set;

import static org.example.common.MessageApplicationError.NOT_INVOLVED_REQUESTER;

@Component
@RequiredArgsConstructor
public class ChatAccessValidator {
    private final ChatFacade chatFacade;

    public Set<Long> getParticipantsIfAllowed(Long chatId, Long requesterId) {
        var participants = chatFacade.findChatParticipants(chatId);
        validateRequester(participants, requesterId);
        return participants;
    }

    private void validateRequester(Set<Long> chatParticipantIds, Long requesterId) {
        if (!chatParticipantIds.contains(requesterId)) {
            throw new ApplicationException(NOT_INVOLVED_REQUESTER);
        }
    }
}
