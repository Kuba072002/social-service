package org.example.application.chat;

import lombok.RequiredArgsConstructor;
import org.example.ApplicationException;
import org.example.application.chat.dto.AddToChatRequest;
import org.example.domain.chat.entity.Chat;
import org.example.domain.chat.service.ChatService;
import org.springframework.stereotype.Service;

import java.util.Set;

import static org.example.common.ChatApplicationError.*;
import static org.example.common.Constants.ADMIN_ROLE;

@Service
@RequiredArgsConstructor
public class AddToChatService {
    private final ChatService chatService;

    public void addToChat(Long userId, AddToChatRequest request) {
        var chat = chatService.findChatWithParticipants(request.chatId())
                .orElseThrow(() -> new ApplicationException(CHAT_NOT_EXISTS));
        validate(userId, request.userIds(), chat);
        chatService.addToChat(userId, chat, request.userIds());
    }

    private void validate(Long userId, Set<Long> userIds, Chat chat) {
        if (chat.getIsPrivate()) {
            throw new ApplicationException(CANNOT_ADD_TO_PRIVATE_CHAT);
        }
        if (!validateIfUserIsAdmin(userId, chat)) {
            throw new ApplicationException(USER_IS_NOT_ADMIN);
        }
        if (checkIfAnyParticipantAlreadyExists(userIds, chat)) {
            throw new ApplicationException(CHAT_PARTICIPANTS_ALREADY_EXISTS);
        }
    }

    private boolean checkIfAnyParticipantAlreadyExists(Set<Long> userIds, Chat chat) {
        return chat.getParticipants().stream()
                .anyMatch(chatParticipant ->
                        userIds.contains(chatParticipant.getUserId()));
    }

    private boolean validateIfUserIsAdmin(Long userId, Chat chat) {
        return chat.getParticipants().stream()
                .anyMatch(chatParticipant -> chatParticipant.getUserId().equals(userId)
                        && chatParticipant.getRole().equals(ADMIN_ROLE));
    }
}
