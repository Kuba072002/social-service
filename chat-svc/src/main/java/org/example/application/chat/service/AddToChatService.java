package org.example.application.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.ApplicationException;
import org.example.application.chat.dto.AddToChatRequest;
import org.example.domain.chat.ChatFacade;
import org.example.domain.chat.entity.Chat;
import org.example.domain.chat.entity.ChatParticipant;
import org.example.domain.event.ChatEvent;
import org.example.domain.event.ChatEventPublisher;
import org.example.domain.user.UserFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.example.common.ChatApplicationError.*;
import static org.example.common.Constants.ADMIN_ROLE;

@Service
@RequiredArgsConstructor
public class AddToChatService {
    private final ChatFacade chatFacade;
    private final UserFacade userFacade;
    private final ChatEventPublisher chatEventPublisher;

    @Transactional
    public void addToChat(Long userId, AddToChatRequest request) {
        var chat = validateAndGetChat(userId, request);
        var chatParticipants = request.userIds().stream()
                .map(id -> new ChatParticipant(chat, id))
                .toList();
        chatFacade.saveParticipants(chatParticipants);
        chatEventPublisher.sendEvent(ChatEvent.addParticipantsEvent(chat.getId(), chatParticipants));
    }

    private Chat validateAndGetChat(Long userId, AddToChatRequest request) {
        if (request.userIds().contains(userId)) {
            throw new ApplicationException(CANNOT_ADD_YOURSELF_TO_CHAT);
        }
        var chat = chatFacade.findChatWithParticipants(request.chatId())
                .orElseThrow(() -> new ApplicationException(CHAT_NOT_EXISTS));
        if (chat.getIsPrivate()) {
            throw new ApplicationException(CANNOT_ADD_TO_PRIVATE_CHAT);
        }
        validateIfUserIsAdmin(userId, chat);
        checkIfAnyParticipantAlreadyExists(request.userIds(), chat);
        userFacade.validateUsers(request.userIds());
        return chat;
    }

    private void checkIfAnyParticipantAlreadyExists(Set<Long> userIds, Chat chat) {
        var anyParticipantAlreadyExists = chat.getParticipants().stream()
                .anyMatch(chatParticipant ->
                        userIds.contains(chatParticipant.getUserId()));
        if (anyParticipantAlreadyExists) {
            throw new ApplicationException(CHAT_PARTICIPANTS_ALREADY_EXISTS);
        }
    }

    private void validateIfUserIsAdmin(Long userId, Chat chat) {
        var userIsAdmin = chat.getParticipants().stream()
                .anyMatch(chatParticipant -> chatParticipant.getUserId().equals(userId)
                        && chatParticipant.getRole().equals(ADMIN_ROLE));
        if (!userIsAdmin) {
            throw new ApplicationException(USER_IS_NOT_ADMIN);
        }
    }
}
