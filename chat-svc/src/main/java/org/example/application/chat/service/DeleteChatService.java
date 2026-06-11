package org.example.application.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.ApplicationException;
import org.example.domain.chat.ChatFacade;
import org.example.domain.chat.entity.ChatParticipantRole;
import org.example.domain.chat.entity.ChatType;
import org.springframework.stereotype.Service;

import static org.example.common.ChatApplicationError.CANNOT_MODIFY_PRIVATE_CHAT;
import static org.example.common.ChatApplicationError.CHAT_NOT_EXISTS;
import static org.example.common.ChatApplicationError.USER_DOES_NOT_BELONG_TO_CHAT;
import static org.example.common.ChatApplicationError.USER_IS_NOT_ADMIN;

@Service
@RequiredArgsConstructor
public class DeleteChatService {
    private final ChatFacade chatFacade;

    public void delete(Long userId, Long chatId) {
        var participant = chatFacade.getChatParticipant(chatId, userId)
                .orElseThrow(() -> new ApplicationException(USER_DOES_NOT_BELONG_TO_CHAT));
        if (participant.getRole() != ChatParticipantRole.ADMIN
                && participant.getRole() != ChatParticipantRole.OWNER) {
            throw new ApplicationException(USER_IS_NOT_ADMIN);
        }
        chatFacade.delete(chatId);
    }

    public void deleteParticipant(Long userId, Long chatId) {
        var chat = chatFacade.findChatWithParticipants(chatId)
                .orElseThrow(() -> new ApplicationException(CHAT_NOT_EXISTS));
        var chatParticipant = chat.getParticipants().stream()
                .filter(participant -> participant.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ApplicationException(USER_DOES_NOT_BELONG_TO_CHAT));
        //TODO add check if there will be other user with ADMIN role
        if (chat.getChatType() == ChatType.PRIVATE) {
            throw new ApplicationException(CANNOT_MODIFY_PRIVATE_CHAT);
        }
        chatFacade.deleteParticipant(chat, chatParticipant);
    }
}
