package org.example.application.chat;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.application.chat.dto.ChatParticipantsDTO;
import org.example.application.chat.dto.ChatRequest;
import org.example.application.chat.dto.ChatsResponse;
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

    @GetMapping("/chat")
    public ResponseEntity<ChatsResponse> getUserChats(
            @RequestHeader Long userId
    ) {
        return ResponseEntity.ok(chatService.getChats(userId));
    }

    @GetMapping("/internal/chat")
    public ResponseEntity<ChatParticipantsDTO> findChatParticipants(@RequestParam Long chatId) {
        return ResponseEntity.ok()
                .body(chatService.findChatParticipants(chatId));
    }
}
