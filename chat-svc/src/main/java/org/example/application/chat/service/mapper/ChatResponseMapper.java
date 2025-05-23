package org.example.application.chat.service.mapper;

import org.example.application.chat.dto.ChatDTO;
import org.example.application.chat.dto.ChatsResponse;
import org.example.application.chat.dto.ParticipantDTO;
import org.example.domain.chat.entity.Chat;
import org.example.domain.chat.entity.ChatParticipant;
import org.example.domain.user.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.util.List;

@Mapper
public interface ChatResponseMapper {
    default ChatsResponse toChatsResponse(List<ChatParticipant> chats) {
        return new ChatsResponse(chats.stream()
                .map(cp -> toChatDTO(cp.getChat(), cp.getLastReadAt()))
                .toList());
    }

    ChatDTO toChatDTO(Chat chat, Instant lastReadAt);

    @Mapping(source = "chatParticipant.chat.id", target = "id")
    @Mapping(source = "userDTO.userName", target = "name")
    @Mapping(source = "userDTO.imageUrl", target = "imageUrl")
    @Mapping(source = "userDTO.id", target = "userId")
    @Mapping(source = "chatParticipant.chat.isPrivate", target = "isPrivate")
    @Mapping(source = "chatParticipant.lastReadAt", target = "lastReadAt")
    @Mapping(source = "chatParticipant.chat.lastMessageAt", target = "lastMessageAt")
    ChatDTO toChatDTO(ChatParticipant chatParticipant, UserDTO userDTO);

    @Mapping(source = "chatParticipant.userId", target = "userId")
    @Mapping(source = "userDTO.userName", target = "userName")
    @Mapping(source = "userDTO.imageUrl", target = "imageUrl")
    @Mapping(source = "chatParticipant.role", target = "role")
    @Mapping(source = "chatParticipant.joinedAt", target = "joinedAt")
    ParticipantDTO toParticipantDto(ChatParticipant chatParticipant, UserDTO userDTO);
}
