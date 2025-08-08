package org.example.application.chat;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.example.application.chat.service.CreateChatService;
import org.example.application.chat.service.DeleteChatService;
import org.example.application.chat.service.GetChatService;
import org.example.application.chat.service.ModifyChatService;
import org.example.dto.chat.*;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

@CrossOrigin
@Controller
@RequiredArgsConstructor
public class ChatController {
    private final CreateChatService createChatService;
    private final ModifyChatService modifyChatService;
    private final GetChatService getChatService;
    private final DeleteChatService deleteChatService;

    @MutationMapping("createChat")
    public Long createChat(
            @Argument Long userId,
            @Argument ChatRequest chatRequest
    ) {
        return createChatService.create(userId, chatRequest);
    }

    @MutationMapping("modifyChat")
    public Boolean modifyChat(
            @Argument Long userId,
            @Argument Long chatId,
            @Argument ModifyChatRequest modifyChatRequest
    ) {
        modifyChatService.modifyChat(userId, chatId, modifyChatRequest);
        return true;
    }

    @MutationMapping("modifyChatParticipants")
    public Boolean modifyChatParticipants(
            @Argument Long userId,
            @Argument Long chatId,
            @Argument ModifyChatParticipantsRequest modifyChatParticipants
    ) {
        modifyChatService.modifyChatParticipants(userId, chatId, modifyChatParticipants);
        return true;
    }

    @MutationMapping("deleteParticipant")
    public Boolean deleteParticipant(
            @Argument Long userId,
            @Argument Long chatId
    ) {
        deleteChatService.deleteParticipant(userId, chatId);
        return true;
    }

    @QueryMapping("getUserChats")
    public List<ChatDetail> getUserChats(
            @Argument Long userId,
            @Argument boolean isPrivate,
            @Argument @Min(1) Integer pageNumber,
            @Argument @Min(1) Integer pageSize
    ) {
        return getChatService.getChats(userId, isPrivate, pageNumber, pageSize);
    }

    @QueryMapping("getChatParticipants")
    public List<ParticipantDTO> getChatParticipants(
            @Argument Long userId,
            @Argument Long chatId
    ) {
        return getChatService.getParticipants(userId, chatId);
    }

    @MutationMapping("updateLastReadAt")
    public Boolean updateLastReadAt(
            @Argument Long userId,
            @Argument Long chatId,
            @Argument UpdateChatReadAtRequest request
    ) {
        modifyChatService.updateLastReadAt(userId, chatId, request.lastReadAt().toInstant());
        return true;
    }

    @MutationMapping("deleteChat")
    public Boolean deleteChat(
            @Argument Long userId,
            @Argument Long chatId
    ) {
        deleteChatService.delete(userId, chatId);
        return true;
    }

    @GetMapping("/internal/chats/participants/ids")
    public ResponseEntity<Set<Long>> getChatParticipantsIds(@RequestParam Long chatId) {
        return ResponseEntity.ok()
                .body(getChatService.getParticipantIds(chatId));
    }
}
