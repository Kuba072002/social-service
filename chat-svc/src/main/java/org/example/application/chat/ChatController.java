package org.example.application.chat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.example.application.chat.dto.*;
import org.example.application.chat.service.CreateChatService;
import org.example.application.chat.service.DeleteChatService;
import org.example.application.chat.service.GetChatService;
import org.example.application.chat.service.ModifyChatService;
import org.example.domain.chat.entity.ChatDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

import static org.springframework.http.HttpStatus.CREATED;

@CrossOrigin
@RestController
@RequiredArgsConstructor
@Validated
public class ChatController {
    private final CreateChatService createChatService;
    private final ModifyChatService modifyChatService;
    private final GetChatService getChatService;
    private final DeleteChatService deleteChatService;

    @PostMapping("/chats")
    public ResponseEntity<Long> createChat(
            @RequestHeader Long userId,
            @RequestBody @Valid ChatRequest chatRequest
    ) {
        return ResponseEntity.status(CREATED)
                .body(createChatService.create(userId, chatRequest));
    }

    @PutMapping("/chats/{chatId}")
    public ResponseEntity<Long> modifyChat(
            @RequestHeader Long userId,
            @PathVariable Long chatId,
            @RequestBody @Valid ModifyChatRequest modifyChatRequest
    ) {
        modifyChatService.modifyChat(userId, chatId, modifyChatRequest);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/chats/{chatId}/participants")
    public ResponseEntity<Void> modifyChatParticipants(
            @RequestHeader Long userId,
            @PathVariable Long chatId,
            @RequestBody @Valid ModifyChatParticipantsRequest modifyChatParticipantsRequest
    ) {
        modifyChatService.modifyChatParticipants(userId, chatId, modifyChatParticipantsRequest);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/chats/{chatId}/participants")
    public ResponseEntity<Void> deleteParticipant(
            @RequestHeader Long userId,
            @PathVariable Long chatId
    ) {
        deleteChatService.deleteParticipant(userId, chatId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/chats")
    public ResponseEntity<List<ChatDetail>> getUserChats(
            @RequestHeader Long userId,
            @RequestParam(required = false) boolean isPrivate,
            @RequestParam(required = false, defaultValue = "1") @Min(1) Integer pageNumber,
            @RequestParam(required = false, defaultValue = "${default.chat.page.size}") @Min(1) Integer pageSize
    ) {
        return ResponseEntity.ok(getChatService.getChats(userId, isPrivate, pageNumber, pageSize));
    }

    @GetMapping("/chats/{chatId}/participants")
    public ResponseEntity<List<ParticipantDTO>> getChatParticipants(
            @RequestHeader Long userId,
            @PathVariable Long chatId
    ) {
        return ResponseEntity.ok(getChatService.getParticipants(userId, chatId));
    }

    @PutMapping("/chats/{chatId}/participants/last_read_at")
    public ResponseEntity<Void> updateLastReadAt(
            @RequestHeader Long userId,
            @PathVariable Long chatId,
            @RequestBody @Valid UpdateChatReadAtRequest request
    ) {
        modifyChatService.updateLastReadAt(userId, chatId, request.lastReadAt());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/chats/{chatId}")
    public ResponseEntity<Void> deleteChat(
            @RequestHeader Long userId,
            @PathVariable Long chatId
    ) {
        deleteChatService.delete(userId, chatId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/internal/chats/participants/ids")
    public ResponseEntity<Set<Long>> getChatParticipantsIds(@RequestParam Long chatId) {
        return ResponseEntity.ok()
                .body(getChatService.getParticipantIds(chatId));
    }
}
