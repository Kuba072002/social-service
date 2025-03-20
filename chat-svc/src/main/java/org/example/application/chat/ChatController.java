package org.example.application.chat;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.application.chat.dto.ChatRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
public class ChatController {
    private final CreateChatService createChatService;

    public ResponseEntity<Long> createChat(
            @RequestHeader Long userId,
            @RequestBody @Valid ChatRequest chatRequest) {
        return ResponseEntity.status(CREATED)
                .body(createChatService.create(userId, chatRequest));
    }
}
