package org.example.application.chat;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.application.chat.dto.ChatRequest;
import org.example.application.chat.dto.ChatsResponse;
import org.example.application.chat.dto.AddToChatRequest;
import org.example.application.chat.dto.ParticipantDTO;
import org.example.domain.chat.ChatFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
public class ChatController {
    private final CreateChatService createChatService;
    private final AddToChatService addToChatService;
    private final ChatFacade chatFacade;
    private final GetChatParticipantsService getChatParticipantsService;
    private final ChatResponseMapper chatResponseMapper;

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
        var response = chatResponseMapper.toChatResponse(chatFacade.getChats(userId));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/chats/{id}/participants")
    public ResponseEntity<List<ParticipantDTO>> getChatParticipants(
            @RequestHeader Long userId,
            @RequestParam Long chatId
    ) {
        return ResponseEntity.ok(getChatParticipantsService.get(userId,chatId));
    }

    @GetMapping("/internal/chats/participants/ids")
    public ResponseEntity<Set<Long>> getChatParticipantsIds(@RequestParam Long chatId) {
        return ResponseEntity.ok()
                .body(getChatParticipantsService.getIds(chatId));
    }
}
