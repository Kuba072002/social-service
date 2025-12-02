package org.example.application.chat.service;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.example.ApplicationException;
import org.example.application.chat.dto.ChatRequest;
import org.example.application.chat.service.mapper.ChatMapper;
import org.example.domain.chat.ChatFacade;
import org.example.domain.chat.entity.Chat;
import org.example.domain.chat.entity.ChatParticipant;
import org.example.domain.user.UserFacade;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

import static org.example.BasicApplicationError.INVALID_DATA;
import static org.example.common.ChatApplicationError.*;
import static org.example.common.Constants.ADMIN_ROLE;

@Service
@RequiredArgsConstructor
public class CreateChatService {
    private final UserFacade userFacade;
    private final Validator validator;
    private final ChatFacade chatFacade;
    private final ChatMapper chatMapper;
    private final Map<Boolean, Class<?>> validationGroups = Map.of(
            true, ChatRequest.PrivateChatGroup.class,
            false, ChatRequest.GroupChatGroup.class
    );

    public Long create(Long userId, ChatRequest chatRequest) {
        validate(userId, chatRequest);
        var chat = prepareChatWithParticipants(userId, chatRequest);
        chatFacade.createChat(chat);
        return chat.getId();
    }

    private Chat prepareChatWithParticipants(Long userId, ChatRequest chatRequest) {
        var chat = chatMapper.toChat(chatRequest);
        var chatParticipants = chatMapper.toChatParticipants(chatRequest.userIds(), chat);
        if (BooleanUtils.isTrue(chat.getIsPrivate())) {
            chatParticipants.getFirst().setRole(ADMIN_ROLE);
        }
        chatParticipants.add(new ChatParticipant(chat, userId, ADMIN_ROLE));
        chat.setParticipants(chatParticipants);
        return chat;
    }

    private void validate(Long userId, ChatRequest chatRequest) {
        validateRequest(userId, chatRequest);
        userFacade.validateUsers(chatRequest.userIds());
        if (chatRequest.isPrivate()) {
            validateIfPrivateChatAlreadyExists(userId, chatRequest.userIds().iterator().next());
        }
    }

    private void validateIfPrivateChatAlreadyExists(Long user1Id, Long user2Id) {
        var exists = chatFacade.checkIfPrivateChatExists(user1Id, user2Id);
        if (exists) {
            throw new ApplicationException(PRIVATE_CHAT_ALREADY_EXISTS);
        }
    }

    private void validateRequest(Long userId, ChatRequest chatRequest) {
        if (chatRequest.userIds().contains(userId)) {
            throw new ApplicationException(REQUEST_CANNOT_CONTAIN_REQUESTER_ID);
        }
        var validationResult = Optional.ofNullable(chatRequest.isPrivate())
                .map(validationGroups::get)
                .map(validationGroup -> validator.validate(chatRequest, validationGroup))
                .orElseThrow(() -> new ApplicationException(REQUEST_GROUP_VALIDATION_NOT_EXISTS));
        if (!validationResult.isEmpty()) {
            throw new ApplicationException(INVALID_DATA.formatted(validationResult));
        }
    }
}
