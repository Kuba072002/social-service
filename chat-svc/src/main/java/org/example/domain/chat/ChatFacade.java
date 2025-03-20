package org.example.domain.chat;

import lombok.RequiredArgsConstructor;
import org.example.application.chat.dto.ChatRequest;
import org.example.domain.user.UserFacade;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatFacade {
    private final UserFacade userFacade;

    public void create(Long userId, ChatRequest chatRequest) {
        userFacade.validateUsers(chatRequest.userIds());
    }
}
