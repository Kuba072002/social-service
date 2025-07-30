package org.example.application.message;

import org.example.domain.message.Message;
import org.example.dto.message.MessageDTO;
import org.example.dto.message.MessageRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface MessageMapper {

    @Mapping(target = "messageId", expression = "java(com.github.f4b6a3.uuid.UuidCreator.getTimeOrderedEpoch())")
    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
    Message toMessage(Long senderId, MessageRequest messageRequest);

    @Mapping(target = "createdAt", expression = "java(message.getCreatedAt().atOffset(java.time.ZoneOffset.UTC))")
    MessageDTO toMessageDTO(Message message);
}
