package org.example.application.chat;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.example.ApplicationException;
import org.example.application.chat.dto.ChatRequest;
import org.example.domain.chat.ChatFacade;
import org.example.domain.user.UserFacade;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

import static org.example.BasicApplicationError.INVALID_DATA;
import static org.example.common.ChatApplicationError.*;

@Service
@RequiredArgsConstructor
public class CreateChatService {
    private final UserFacade userFacade;
    private final Validator validator;
    private final ChatFacade chatFacade;
    private final ChatMapper chatMapper;

    public Long create(Long userId, ChatRequest chatRequest) {
        validateRequest(userId, chatRequest);
        userFacade.validateUsers(chatRequest.userIds());
        if (chatRequest.isPrivate()) {
            validateIfPrivateChatAlreadyExists(userId, chatRequest.userIds().iterator().next());
        }
        var chat = chatMapper.toChat(chatRequest);
        chatFacade.createChat(userId, chat, chatRequest.userIds());
        return chat.getId();
    }

    private void validateIfPrivateChatAlreadyExists(Long user1Id, Long user2Id) {
        var exists = chatFacade.checkIfPrivateChatExists(user1Id, user2Id);
        if (exists) {
            throw new ApplicationException(PRIVATE_CHAT_ALREADY_EXISTS);
        }
    }

    private void validateRequest(Long userId, ChatRequest chatRequest) {
        if (chatRequest.userIds().contains(userId)) {
            throw new ApplicationException(CANNOT_ADD_YOURSELF_TO_CHAT);
        }
        var validationGroups = Map.of(
                true, ChatRequest.PrivateChatGroup.class,
                false, ChatRequest.GroupChatGroup.class
        );
        var validationResult = Optional.ofNullable(chatRequest.isPrivate())
                .map(validationGroups::get)
                .map(validationGroup -> validator.validate(chatRequest, validationGroup))
                .orElseThrow(() -> new ApplicationException(REQUEST_GROUP_VALIDATION_NOT_EXISTS));
        if (!validationResult.isEmpty()) {
            throw new ApplicationException(INVALID_DATA.formatted(validationResult));
        }
    }
}
