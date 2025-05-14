package org.example.application.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.ApplicationException;
import org.example.domain.chat.ChatFacade;
import org.example.domain.event.ChatEvent;
import org.example.domain.event.ChatEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.example.common.ChatApplicationError.USER_DOES_NOT_BELONG_TO_CHAT;

@Service
@RequiredArgsConstructor
public class ChatManagementService {
    private final ChatFacade chatFacade;
    private final ChatEventPublisher chatEventPublisher;

    public void updateLastReadAt(Long userId, Long chatId, Instant lastReadAt) {
        var chatParticipant = chatFacade.getChatParticipant(chatId, userId)
                .orElseThrow(() -> new ApplicationException(USER_DOES_NOT_BELONG_TO_CHAT));
        if (chatParticipant.getLastReadAt() == null
                || lastReadAt.isAfter(chatParticipant.getLastReadAt())) {
            chatParticipant.setLastReadAt(lastReadAt);
            chatFacade.save(chatParticipant);
        }
    }

    @Transactional
    public void delete(Long userId, Long chatId) {
        chatFacade.getChatParticipant(chatId, userId)
                .orElseThrow(() -> new ApplicationException(USER_DOES_NOT_BELONG_TO_CHAT));
        chatFacade.delete(chatId);
        chatEventPublisher.sendEvent(ChatEvent.deleteEvent(chatId));
    }
}
