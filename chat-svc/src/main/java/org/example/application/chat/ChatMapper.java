package org.example.application.chat;

import org.example.application.chat.dto.ChatRequest;
import org.example.domain.chat.entity.Chat;
import org.mapstruct.Mapper;

@Mapper
public interface ChatMapper {

    Chat toChat(ChatRequest chatRequest);
}
