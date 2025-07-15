package org.example.application.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.ApplicationException;
import org.example.domain.chat.ChatFacade;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static org.example.common.ChatApplicationError.*;
import static org.example.common.Constants.ADMIN_ROLE;

@Service
@RequiredArgsConstructor
public class DeleteChatService {
    private final ChatFacade chatFacade;

    public void delete(Long userId, Long chatId) {
        var participant = chatFacade.getChatParticipant(chatId, userId)
                .orElseThrow(() -> new ApplicationException(USER_DOES_NOT_BELONG_TO_CHAT));
        if (!Objects.equals(participant.getRole(), ADMIN_ROLE)) {
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
        if (chat.getIsPrivate()) {
            throw new ApplicationException(CANNOT_MODIFY_PRIVATE_CHAT);
        }
        chatFacade.deleteParticipant(chat, chatParticipant);
    }
}
