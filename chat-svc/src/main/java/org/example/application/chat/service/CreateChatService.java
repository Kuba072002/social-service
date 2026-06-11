package org.example.application.chat.service;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.example.ApplicationException;
import org.example.application.chat.dto.ChatRequest;
import org.example.application.chat.service.mapper.ChatMapper;
import org.example.domain.chat.ChatFacade;
import org.example.domain.chat.entity.Chat;
import org.example.domain.chat.entity.ChatParticipant;
import org.example.domain.chat.entity.ChatParticipantRole;
import org.example.domain.user.UserFacade;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.example.BasicApplicationError.INVALID_DATA;
import static org.example.common.ChatApplicationError.PRIVATE_CHAT_ALREADY_EXISTS;
import static org.example.common.ChatApplicationError.REQUEST_CANNOT_CONTAIN_REQUESTER_ID;

@Service
@RequiredArgsConstructor
public class CreateChatService {
    private final UserFacade userFacade;
    private final Validator validator;
    private final ChatFacade chatFacade;
    private final ChatMapper chatMapper;
    private final Map<ChatRequest.ChatType, Class<?>> validationGroups = Map.of(
            ChatRequest.ChatType.PRIVATE, ChatRequest.PrivateChatGroup.class,
            ChatRequest.ChatType.GROUP, ChatRequest.GroupChatGroup.class
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
        if (chatRequest.chatType() == ChatRequest.ChatType.PRIVATE) {
            chatParticipants.getFirst().setRole(ChatParticipantRole.ADMIN);
            chat.setPrivatePairKey(getPrivatePairKey(userId, chatRequest.userIds().iterator().next()));
        }
        chatParticipants.add(new ChatParticipant(chat, userId, ChatParticipantRole.OWNER));
        chat.setParticipants(chatParticipants);
        return chat;
    }

    private void validate(Long userId, ChatRequest chatRequest) {
        validateRequest(userId, chatRequest);
        userFacade.getAndValidateUsers(chatRequest.userIds());
        if (chatRequest.chatType() == ChatRequest.ChatType.PRIVATE) {
            validateIfPrivateChatAlreadyExists(userId, chatRequest.userIds().iterator().next());
        }
    }

    private void validateIfPrivateChatAlreadyExists(Long user1Id, Long user2Id) {
        var exists = chatFacade.checkIfPrivateChatExists(getPrivatePairKey(user1Id, user2Id));
        if (exists) {
            throw new ApplicationException(PRIVATE_CHAT_ALREADY_EXISTS);
        }
    }

    private void validateRequest(Long userId, ChatRequest chatRequest) {
        if (chatRequest.userIds().contains(userId)) {
            throw new ApplicationException(REQUEST_CANNOT_CONTAIN_REQUESTER_ID);
        }
        var validationResult = validator.validate(chatRequest, validationGroups.get(chatRequest.chatType()));
        if (!validationResult.isEmpty()) {
            throw new ApplicationException(INVALID_DATA.formatted(validationResult));
        }
    }

    private static String getPrivatePairKey(Long user1Id, Long user2Id) {
        return Stream.of(user1Id, user2Id)
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(":"));
    }
}
