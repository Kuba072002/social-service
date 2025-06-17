package org.example.application.chat.service.mapper;

import org.example.application.chat.dto.ParticipantDTO;
import org.example.domain.chat.entity.ChatParticipant;
import org.example.domain.user.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ChatResponseMapper {

    @Mapping(source = "chatParticipant.userId", target = "userId")
    @Mapping(source = "userDTO.userName", target = "userName")
    @Mapping(source = "userDTO.imageUrl", target = "imageUrl")
    @Mapping(source = "chatParticipant.role", target = "role")
    @Mapping(source = "chatParticipant.joinedAt", target = "joinedAt")
    ParticipantDTO toParticipantDto(ChatParticipant chatParticipant, UserDTO userDTO);
}
