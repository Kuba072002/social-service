package org.example.application.chat;

import lombok.RequiredArgsConstructor;
import org.example.ApplicationException;
import org.example.application.chat.dto.AddToChatRequest;
import org.example.domain.chat.entity.Chat;
import org.example.domain.chat.entity.ChatParticipant;
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
        if (validateIfUserIsAdmin(userId,chat)){
            throw new ApplicationException(USER_IS_NOT_ADMIN);
        }
        if (checkIfAnyParticipantAlreadyExists(request.userIds(), chat)) {
            throw new ApplicationException(CHAT_PARTICIPANTS_ALREADY_EXISTS);
        }
        chatService.addToChat(userId, chat, request.userIds());
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
