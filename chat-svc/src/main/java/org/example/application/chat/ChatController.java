package org.example.application.chat;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.application.chat.dto.ChatDTO;
import org.example.application.chat.dto.ChatRequest;
import org.example.domain.chat.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
public class ChatController {
    private final CreateChatService createChatService;
    private final ChatService chatService;

    @PostMapping("/chat")
    public ResponseEntity<Long> createChat(
            @RequestHeader Long userId,
            @RequestBody @Valid ChatRequest chatRequest) {
        return ResponseEntity.status(CREATED)
                .body(createChatService.create(userId, chatRequest));
    }

    @GetMapping("/internal/chat")
    public ResponseEntity<ChatDTO> getChatParticipants(@RequestParam Long chatId) {
        return ResponseEntity.ok()
                .body(chatService.getChat(chatId));
    }
}
