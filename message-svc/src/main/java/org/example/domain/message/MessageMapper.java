package org.example.domain.message;

import org.example.application.MessageDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface MessageMapper {
    @Mapping(target = "createdAt",expression = "java(java.time.Instant.now())")
    Message toMessage(Long senderId, MessageDTO messageDTO);
}
