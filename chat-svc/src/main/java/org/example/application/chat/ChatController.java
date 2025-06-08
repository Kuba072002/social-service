package org.example.application.chat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.example.application.chat.dto.ChatRequest;
import org.example.application.chat.dto.ChatsResponse;
import org.example.application.chat.dto.ParticipantDTO;
import org.example.application.chat.dto.UpdateChatReadAtRequest;
import org.example.application.chat.service.AddToChatService;
import org.example.application.chat.service.ChatManagementService;
import org.example.application.chat.service.CreateChatService;
import org.example.application.chat.service.GetChatDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

import static org.springframework.http.HttpStatus.CREATED;

@CrossOrigin
@RestController
@RequiredArgsConstructor
public class ChatController {
    private final CreateChatService createChatService;
    private final AddToChatService addToChatService;
    private final GetChatDetailsService getChatDetailsService;
    private final ChatManagementService chatManagementService;

    @PostMapping("/chats")
    public ResponseEntity<Long> createChat(
            @RequestHeader Long userId,
            @RequestBody @Valid ChatRequest chatRequest
    ) {
        return ResponseEntity.status(CREATED)
                .body(createChatService.create(userId, chatRequest));
    }

    @PutMapping("/chats/{chatId}/participants")
    public ResponseEntity<Void> addToChat(
            @RequestHeader Long userId,
            @PathVariable Long chatId,
            @RequestBody @Valid @NotEmpty Set<Long> userIds
    ) {
        addToChatService.addToChat(userId, chatId, userIds);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/chats/{chatId}/participants")
    public ResponseEntity<Void> deleteParticipant(
            @RequestHeader Long userId,
            @PathVariable Long chatId
    ) {
        chatManagementService.deleteParticipant(userId, chatId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/chats")
    public ResponseEntity<ChatsResponse> getUserChats(
            @RequestHeader Long userId,
            @RequestParam(required = false) boolean isPrivate,
            @RequestParam(required = false) @Min(0) Integer pageNumber,
            @RequestParam(required = false) @Min(1) Integer pageSize
    ) {
        return ResponseEntity.ok(getChatDetailsService.getChats(userId, isPrivate, pageNumber, pageSize));
    }

    @GetMapping("/chats/{chatId}/participants")
    public ResponseEntity<List<ParticipantDTO>> getChatParticipants(
            @RequestHeader Long userId,
            @PathVariable Long chatId
    ) {
        return ResponseEntity.ok(getChatDetailsService.getParticipants(userId, chatId));
    }

    @PutMapping("/chats/{chatId}/participants/last_read_at")
    public ResponseEntity<Void> updateLastReadAt(
            @RequestHeader Long userId,
            @PathVariable Long chatId,
            @RequestBody UpdateChatReadAtRequest request
    ) {
        chatManagementService.updateLastReadAt(userId, chatId, request.lastReadAt());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/chats/{chatId}")
    public ResponseEntity<Void> deleteChat(
            @RequestHeader Long userId,
            @PathVariable Long chatId
    ) {
        chatManagementService.delete(userId, chatId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/internal/chats/participants/ids")
    public ResponseEntity<Set<Long>> getChatParticipantsIds(@RequestParam Long chatId) {
        return ResponseEntity.ok()
                .body(getChatDetailsService.getParticipantIds(chatId));
    }
}
