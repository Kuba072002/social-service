package org.example.application.message.service;

import org.example.application.dto.MessageDTO;
import org.example.domain.message.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface MessageMapper {

    @Mapping(target = "messageId", expression = "java(com.github.f4b6a3.uuid.UuidCreator.getTimeOrderedEpoch())")
    @Mapping(target = "timestamp", expression = "java(java.time.Instant.now())")
    @Mapping(target = "state", constant = "CREATED")
    Message toMessage(Long senderId, Long chatId, String content);

    MessageDTO toMessageDTO(Message message);
}
