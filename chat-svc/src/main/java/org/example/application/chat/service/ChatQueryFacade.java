package org.example.application.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.application.chat.dto.ChatTypeDTO;
import org.example.application.chat.dto.ParticipantDTO;
import org.example.domain.chat.projection.ChatDetail;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatQueryFacade {
    private final GetChatService getChatService;
    private final GetChatsService getChatsService;

    public List<ChatDetail> getChats(Long userId, ChatTypeDTO chatType, Integer pageNumber, Integer pageSize) {
        return getChatsService.getChats(userId, chatType, pageNumber, pageSize);
    }

    public ChatDetail getChat(Long userId, Long chatId) {
        return getChatService.getChat(userId, chatId);
    }

    public List<ParticipantDTO> getParticipants(Long userId, Long chatId) {
        return getChatService.getParticipants(userId, chatId);
    }

    public List<Long> getChatParticipantsIds(Long chatId) {
        return getChatService.getChatParticipantsIds(chatId);
    }
}
