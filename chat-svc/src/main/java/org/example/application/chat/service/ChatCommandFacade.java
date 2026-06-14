package org.example.application.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.application.chat.dto.ChatRequest;
import org.example.application.chat.dto.ModifyChatParticipantRole;
import org.example.application.chat.dto.ModifyChatParticipantsRequest;
import org.example.application.chat.dto.ModifyChatRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ChatCommandFacade {
    private final CreateChatService createChatService;
    private final ModifyChatService modifyChatService;
    private final DeleteChatService deleteChatService;

    public Long createChat(Long userId, ChatRequest chatRequest) {
        return createChatService.create(userId, chatRequest);
    }

    public void modifyChatParticipants(Long userId, Long chatId, ModifyChatParticipantsRequest modifyRequest) {
        modifyChatService.modifyChatParticipants(userId, chatId, modifyRequest);
    }

    public void updateLastReadAt(Long userId, Long chatId, Instant lastReadAt) {
        modifyChatService.updateLastReadAt(userId, chatId, lastReadAt);
    }

    public void modifyChat(Long userId, Long chatId, ModifyChatRequest modifyChatRequest) {
        modifyChatService.modifyChat(userId, chatId, modifyChatRequest);
    }

    public void modifyChatParticipantRole(Long userId, Long chatId, ModifyChatParticipantRole modifyChatParticipantRole) {
        modifyChatService.modifyChatParticipantRole(userId, chatId, modifyChatParticipantRole);
    }

    public void deleteChat(Long userId, Long chatId) {
        deleteChatService.delete(userId, chatId);
    }

    public void deleteParticipant(Long userId, Long chatId) {
        deleteChatService.deleteParticipant(userId, chatId);
    }
}
