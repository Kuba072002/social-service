package org.example.application.chat;

import org.example.application.chat.dto.ChatDTO;
import org.example.application.chat.dto.ChatParticipantsDTO;
import org.example.application.chat.dto.ChatRequest;
import org.example.application.chat.dto.ChatsResponse;
import org.example.domain.chat.entity.Chat;
import org.example.domain.chat.entity.ChatParticipant;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface ChatMapper {

    Chat toChat(ChatRequest chatRequest);

    default ChatParticipantsDTO toChatParticipantsDTO(List<ChatParticipant> chatParticipants) {
        return new ChatParticipantsDTO(chatParticipants.stream()
                .map(ChatParticipant::getUserId)
                .collect(Collectors.toSet()));
    }

    default ChatsResponse toChatResponse(List<Chat> chats) {
        return new ChatsResponse(chats.stream()
                .map(this::toChatDTO)
                .toList());
    }

    ChatDTO toChatDTO(Chat chat);
}
