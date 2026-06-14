package org.example.application.chat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.example.application.chat.dto.ChatRequest;
import org.example.application.chat.dto.ChatTypeDTO;
import org.example.application.chat.dto.ModifyChatParticipantRole;
import org.example.application.chat.dto.ModifyChatParticipantsRequest;
import org.example.application.chat.dto.ModifyChatRequest;
import org.example.application.chat.dto.ParticipantDTO;
import org.example.application.chat.dto.UpdateChatReadAtRequest;
import org.example.application.chat.service.ChatCommandFacade;
import org.example.application.chat.service.ChatQueryFacade;
import org.example.domain.chat.projection.ChatDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.example.common.Constants.USER_ID_HEADER;
import static org.springframework.http.HttpStatus.CREATED;

@CrossOrigin
@RestController
@RequiredArgsConstructor
@Validated
public class ChatController {
    private final ChatCommandFacade chatCommandFacade;
    private final ChatQueryFacade chatQueryFacade;

    @PostMapping("/chats")
    public ResponseEntity<Long> createChat(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestBody @Valid ChatRequest chatRequest
    ) {
        return ResponseEntity.status(CREATED)
                .body(chatCommandFacade.createChat(userId, chatRequest));
    }

    @PatchMapping("/chats/{chatId}")
    public ResponseEntity<Void> modifyChat(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable Long chatId,
            @RequestBody @Valid ModifyChatRequest modifyChatRequest
    ) {
        chatCommandFacade.modifyChat(userId, chatId, modifyChatRequest);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/chats/{chatId}/participants")
    public ResponseEntity<Void> modifyChatParticipants(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable Long chatId,
            @RequestBody @Valid ModifyChatParticipantsRequest modifyChatParticipantsRequest
    ) {
        chatCommandFacade.modifyChatParticipants(userId, chatId, modifyChatParticipantsRequest);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/chats/{chatId}/participants/role")
    public ResponseEntity<Void> modifyChatParticipantRole(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable Long chatId,
            @RequestBody @Valid ModifyChatParticipantRole modifyChatParticipantRole
    ) {
        chatCommandFacade.modifyChatParticipantRole(userId, chatId, modifyChatParticipantRole);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/chats/{chatId}/participants")
    public ResponseEntity<Void> deleteParticipant(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable Long chatId
    ) {
        chatCommandFacade.deleteParticipant(userId, chatId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/chats")
    public ResponseEntity<List<ChatDetail>> getUserChats(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestParam(required = false) ChatTypeDTO chatType,
            @RequestParam(required = false, defaultValue = "1") @Min(1) Integer pageNumber,
            @RequestParam(required = false, defaultValue = "${default.chat.page.size}") @Min(1) Integer pageSize
    ) {
        return ResponseEntity.ok(chatQueryFacade.getChats(userId, chatType, pageNumber, pageSize));
    }

    @GetMapping("/chats/{chatId}")
    public ResponseEntity<ChatDetail> getChat(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable Long chatId
    ) {
        return ResponseEntity.ok(chatQueryFacade.getChat(userId, chatId));
    }

    @Deprecated
    @GetMapping("/chats/{chatId}/participants")
    public ResponseEntity<List<ParticipantDTO>> getChatParticipants(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable Long chatId
    ) {
        return ResponseEntity.ok(chatQueryFacade.getParticipants(userId, chatId));
    }

    @Deprecated
    @PatchMapping("/chats/{chatId}/participants/last_read_at")
    public ResponseEntity<Void> updateLastReadAt(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable Long chatId,
            @RequestBody @Valid UpdateChatReadAtRequest request
    ) {
        chatCommandFacade.updateLastReadAt(userId, chatId, request.lastReadAt());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/chats/{chatId}")
    public ResponseEntity<Void> deleteChat(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable Long chatId
    ) {
        chatCommandFacade.deleteChat(userId, chatId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/internal/chats/{chatId}/participants/ids")
    public ResponseEntity<List<Long>> getChatParticipantsIds(@PathVariable Long chatId) {
        return ResponseEntity.ok()
                .body(chatQueryFacade.getChatParticipantsIds(chatId));
    }
}
