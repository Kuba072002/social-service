package org.example.application.chat;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.application.chat.dto.ChatRequest;
import org.example.domain.chat.service.ChatFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatController {
    private final ChatFacade chatFacade;

    public ResponseEntity<Void> createChat(
            @RequestHeader Long userId,
            @RequestBody @Valid ChatRequest chatRequest) {
        chatFacade.create(userId, chatRequest);
        return ResponseEntity.ok().build();
    }
}
