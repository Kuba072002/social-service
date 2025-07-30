package org.example.application.chat.service.mapper;

import org.example.domain.chat.entity.Chat;
import org.example.domain.chat.entity.ChatParticipant;
import org.example.dto.chat.ChatRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper
public interface ChatMapper {

    @Mapping(target = "lastMessageAt", expression = "java(java.time.Instant.now())")
    Chat toChat(ChatRequest chatRequest);

    default List<ChatParticipant> toChatParticipants(Set<Long> userIds, Chat chat) {
        return userIds.stream()
                .map(id -> new ChatParticipant(chat, id))
                .collect(Collectors.toList());
    }
}
