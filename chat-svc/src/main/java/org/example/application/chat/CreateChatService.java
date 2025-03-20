package org.example.application.chat;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.example.application.chat.dto.ChatRequest;
import org.example.domain.chat.service.ChatService;
import org.example.domain.user.UserFacade;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CreateChatService {
    private final UserFacade userFacade;
    private final Validator validator;
    private final ChatService chatService;
    private final ChatMapper chatMapper;

    public Long create(Long userId, ChatRequest chatRequest) {
        validateRequest(chatRequest);
        userFacade.validateUsers(chatRequest.userIds());
        if (chatRequest.isPrivate())
            validateIfPrivateChatAlreadyExists(userId, chatRequest.userIds().iterator().next());

        var chat = chatService.createChat(userId, chatMapper.toChat(chatRequest), chatRequest.userIds());
        return chat.getId();
    }

    private void validateIfPrivateChatAlreadyExists(Long user1Id, Long user2Id) {
        var exists = chatService.checkIfPrivateChatExists(user1Id, user2Id);
        if (!exists) {
            throw new RuntimeException("message");
        }
    }

    private void validateRequest(ChatRequest chatRequest) {
        var validationGroups = Map.of(
                true, ChatRequest.PrivateChatGroup.class,
                false, ChatRequest.GroupChatGroup.class
        );
        var validationResult = Optional.ofNullable(chatRequest.isPrivate())
                .map(validationGroups::get)
                .map(validationGroup -> validator.validate(chatRequest, validationGroup))
                .orElseThrow(() -> new RuntimeException("message"));
        if (!validationResult.isEmpty()) {
            throw new RuntimeException("message");
        }
    }
}
