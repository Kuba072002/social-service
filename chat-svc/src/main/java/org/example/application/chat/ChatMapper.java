package org.example.application.chat;

import org.example.application.chat.dto.ChatDTO;
import org.example.application.chat.dto.ChatRequest;
import org.example.domain.chat.entity.Chat;
import org.example.domain.chat.entity.ChatParticipant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper
public interface ChatMapper {

    Chat toChat(ChatRequest chatRequest);

    @Mapping(source = "chat", target = "participantIds", qualifiedByName = "toParticipantIds")
    ChatDTO toChatDTO(Chat chat);

    @Named("toParticipantIds")
    default Set<Long> toParticipantIds(Chat chat) {
        return chat.getParticipants().stream()
                .map(ChatParticipant::getUserId)
                .collect(Collectors.toSet());
    }
}
