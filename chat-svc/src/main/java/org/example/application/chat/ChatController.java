package org.example.application.chat;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.application.chat.dto.ChatParticipantsDTO;
import org.example.application.chat.dto.ChatRequest;
import org.example.application.chat.dto.ChatsResponse;
import org.example.application.chat.dto.AddToChatRequest;
import org.example.domain.chat.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
public class ChatController {
    private final CreateChatService createChatService;
    private final AddToChatService addToChatService;
    private final ChatService chatService;

    @PostMapping("/chats")
    public ResponseEntity<Long> createChat(
            @RequestHeader Long userId,
            @RequestBody @Valid ChatRequest chatRequest
    ) {
        return ResponseEntity.status(CREATED)
                .body(createChatService.create(userId, chatRequest));
    }

    @PutMapping("/chats")
    public ResponseEntity<Void> addToChat(
            @RequestHeader Long userId,
            @RequestBody @Valid AddToChatRequest addToChatRequest
    ) {
        addToChatService.addToChat(userId, addToChatRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/chats")
    public ResponseEntity<ChatsResponse> getUserChats(
            @RequestHeader Long userId
    ) {
        return ResponseEntity.ok(chatService.getChats(userId));
    }

    @GetMapping("/internal/chats")
    public ResponseEntity<ChatParticipantsDTO> findChatParticipants(@RequestParam Long chatId) {
        return ResponseEntity.ok()
                .body(chatService.findChatParticipants(chatId));
    }
}
