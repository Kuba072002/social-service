package org.example.application.chat.service.mapper;

import org.example.domain.chat.entity.ChatParticipant;
import org.example.dto.chat.ParticipantDTO;
import org.example.dto.user.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ChatResponseMapper {

    @Mapping(source = "chatParticipant.userId", target = "userId")
    @Mapping(source = "userDTO.userName", target = "userName")
    @Mapping(source = "userDTO.imageUrl", target = "imageUrl")
    @Mapping(source = "chatParticipant.role", target = "role")
    @Mapping(expression = "java(chatParticipant.getJoinedAt().atOffset(java.time.ZoneOffset.UTC))", target = "joinedAt")
    ParticipantDTO toParticipantDto(ChatParticipant chatParticipant, UserDTO userDTO);
}
