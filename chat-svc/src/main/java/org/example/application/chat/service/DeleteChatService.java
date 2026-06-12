package org.example.application.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.ApplicationException;
import org.example.domain.chat.ChatFacade;
import org.example.domain.chat.entity.Chat;
import org.example.domain.chat.entity.ChatParticipant;
import org.example.domain.chat.entity.ChatParticipantRole;
import org.example.domain.chat.entity.ChatType;
import org.springframework.stereotype.Service;

import static org.example.common.ChatApplicationError.CANNOT_MODIFY_PRIVATE_CHAT;
import static org.example.common.ChatApplicationError.CHAT_NOT_EXISTS;
import static org.example.common.ChatApplicationError.NO_PARTICIPANTS_LEFT_WITH_ADMIN_ROLE;
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
        var senderParticipant = chat.getParticipants().stream()
                .filter(participant -> participant.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ApplicationException(USER_DOES_NOT_BELONG_TO_CHAT));
        isEligibleForDeleteParticipant(senderParticipant, chat);
        chatFacade.deleteParticipant(chat, senderParticipant);
    }

    private void isEligibleForDeleteParticipant(ChatParticipant senderParticipant, Chat chat) {
        if (senderParticipant.getRole() == ChatParticipantRole.ADMIN
                || senderParticipant.getRole() == ChatParticipantRole.OWNER) {
            chat.getParticipants().stream()
                    .filter(participant -> !participant.equals(senderParticipant))
                    .filter(participant -> participant.getRole() == ChatParticipantRole.ADMIN
                            || participant.getRole() == ChatParticipantRole.OWNER)
                    .findFirst()
                    .orElseThrow(() -> new ApplicationException(NO_PARTICIPANTS_LEFT_WITH_ADMIN_ROLE));
        }
        if (chat.getChatType() == ChatType.PRIVATE) {
            throw new ApplicationException(CANNOT_MODIFY_PRIVATE_CHAT);
        }
    }
}
