package org.example.application.chat.service.mapper;

import org.example.application.chat.dto.ParticipantDTO;
import org.example.domain.chat.entity.Chat;
import org.example.domain.chat.entity.ChatParticipant;
import org.example.domain.chat.projection.ChatDetail;
import org.example.domain.user.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mapper
public interface ChatResponseMapper {

    @Mapping(source = "chat.id", target = "chatId")
    @Mapping(source = "chat.name", target = "name")
    @Mapping(source = "chat.imageUrl", target = "imageUrl")
    @Mapping(source = "chat.chatType", target = "chatType")
    @Mapping(source = "chat.lastMessageAt", target = "lastMessageAt")
    @Mapping(source = "senderParticipant.lastReadAt", target = "lastReadAt")
    @Mapping(source = "participants", target = "participants")
    ChatDetail toChatDetail(Chat chat, ChatParticipant senderParticipant, List<ParticipantDTO> participants);

    @Mapping(source = "chatParticipant.userId", target = "userId")
    @Mapping(source = "userDTO.userName", target = "userName")
    @Mapping(source = "userDTO.imageUrl", target = "imageUrl")
    @Mapping(source = "userDTO.lastSeenAt", target = "lastSeenAt")
    @Mapping(source = "chatParticipant.role", target = "role")
    @Mapping(source = "chatParticipant.joinedAt", target = "joinedAt")
    ParticipantDTO toParticipantDto(ChatParticipant chatParticipant, UserDTO userDTO);

    default List<ParticipantDTO> toParticipantDTOs(
            Collection<ChatParticipant> participants, Map<Long, UserDTO> usersMap
    ) {
        return participants.stream()
                .map(chatParticipant -> {
                    var userDTO = usersMap.get(chatParticipant.getUserId());
                    return this.toParticipantDto(chatParticipant, userDTO);
                })
                .toList();
    }
}
