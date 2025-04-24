package org.example.application.chat;

import lombok.RequiredArgsConstructor;
import org.example.ApplicationException;
import org.example.application.chat.dto.AddToChatRequest;
import org.example.domain.chat.ChatFacade;
import org.example.domain.chat.entity.Chat;
import org.springframework.stereotype.Service;

import java.util.Set;

import static org.example.common.ChatApplicationError.*;
import static org.example.common.Constants.ADMIN_ROLE;

@Service
@RequiredArgsConstructor
public class AddToChatService {
    private final ChatFacade chatFacade;

    public void addToChat(Long userId, AddToChatRequest request) {
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
        chatFacade.addToChat(chat, request.userIds());
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
