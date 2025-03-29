package org.example.application.chat;

import org.example.application.chat.dto.ChatDTO;
import org.example.application.chat.dto.ChatParticipantsDTO;
import org.example.application.chat.dto.ChatRequest;
import org.example.domain.chat.entity.Chat;
import org.example.domain.chat.entity.ChatParticipant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper
public interface ChatMapper {

    Chat toChat(ChatRequest chatRequest);

    @Mapping(source = "chatParticipants", target = "userIds", qualifiedByName = "toUserIds")
    ChatParticipantsDTO toChatParticipantsDTO(List<ChatParticipant> chatParticipants);

    @Named("toUserIds")
    default Set<Long> toUserIds(List<ChatParticipant> chatParticipants) {
        return chatParticipants.stream()
                .map(ChatParticipant::getUserId)
                .collect(Collectors.toSet());
    }
}
