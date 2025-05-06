package org.example.application.message;

import org.example.application.dto.MessageDTO;
import org.example.domain.message.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface MessageMapper {
    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
    Message toMessage(Long senderId, MessageDTO messageDTO);
}
