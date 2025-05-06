package org.example.application.chat;

import org.example.application.chat.dto.ChatRequest;
import org.example.domain.chat.entity.Chat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ChatMapper {

    @Mapping(target = "lastMessageAt", expression = "java(java.time.Instant.now())")
    Chat toChat(ChatRequest chatRequest);
}
