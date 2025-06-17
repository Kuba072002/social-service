package org.example.application.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.ApplicationException;
import org.example.application.chat.service.mapper.ChatMapper;
import org.example.domain.chat.ChatFacade;
import org.example.domain.chat.entity.Chat;
import org.example.domain.chat.entity.ChatParticipant;
import org.example.domain.user.UserFacade;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.example.common.ChatApplicationError.*;
import static org.example.common.Constants.ADMIN_ROLE;

@Service
@RequiredArgsConstructor
public class AddToChatService {
    private final ChatFacade chatFacade;
    private final UserFacade userFacade;
    private final ChatMapper chatMapper;

    public void addToChat(Long userId, Long chatId, Set<Long> userIds) {
        if (userIds.contains(userId)) {
            throw new ApplicationException(CANNOT_ADD_YOURSELF_TO_CHAT);
        }
        var chat = chatFacade.findChatWithParticipants(chatId)
                .orElseThrow(() -> new ApplicationException(CHAT_NOT_EXISTS));
        validate(userId, chat, userIds);
        var chatParticipants = chatMapper.toChatParticipants(userIds, chat);
        chatFacade.addParticipants(chat, chatParticipants);
    }

    private void validate(Long userId, Chat chat, Set<Long> userIds) {
        if (chat.getIsPrivate()) {
            throw new ApplicationException(CANNOT_ADD_TO_PRIVATE_CHAT);
        }
        validateIfUserIsAdmin(userId, chat);
        checkIfAnyParticipantAlreadyExists(userIds, chat.getParticipants());
        userFacade.validateUsers(userIds);
    }

    private void checkIfAnyParticipantAlreadyExists(Set<Long> userIds, List<ChatParticipant> chatParticipants) {
        var existedParticipantIds = chatParticipants.stream()
                .map(ChatParticipant::getUserId)
                .collect(Collectors.toSet());
        var anyParticipantAlreadyExists = userIds.stream()
                .anyMatch(existedParticipantIds::contains);
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
