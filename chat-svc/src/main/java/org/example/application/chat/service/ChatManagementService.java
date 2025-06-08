package org.example.application.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.ApplicationException;
import org.example.domain.chat.ChatFacade;
import org.example.domain.event.ChatEvent;
import org.example.domain.event.ChatEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.example.common.ChatApplicationError.*;
import static org.example.common.ChatApplicationError.CANNOT_DELETE_FROM_PRIVATE_CHAT;
import static org.example.common.Constants.ADMIN_ROLE;

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
        var participant = chatFacade.getChatParticipant(chatId, userId)
                .orElseThrow(() -> new ApplicationException(USER_DOES_NOT_BELONG_TO_CHAT));
        if (!participant.getRole().equals(ADMIN_ROLE)) {
            throw new ApplicationException(USER_IS_NOT_ADMIN);
        }
        chatFacade.delete(chatId);
        chatEventPublisher.sendEvent(ChatEvent.deleteEvent(chatId));
    }

    public void deleteParticipant(Long userId, Long chatId) {
        var chat = chatFacade.findChatWithParticipants(chatId)
                .orElseThrow(() -> new ApplicationException(CHAT_NOT_EXISTS));
        var chatParticipant = chat.getParticipants().stream()
                .filter(participant -> participant.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ApplicationException(USER_DOES_NOT_BELONG_TO_CHAT));
        if (chat.getIsPrivate()) {
            throw new ApplicationException(CANNOT_DELETE_FROM_PRIVATE_CHAT);
        }
        chatFacade.delete(chatParticipant);
    }
}
