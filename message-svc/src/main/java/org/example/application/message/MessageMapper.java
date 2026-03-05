package org.example.application.message;

import org.example.application.dto.MessageDTO;
import org.example.application.dto.MessageRequest;
import org.example.domain.message.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface MessageMapper {

    @Mapping(target = "messageId", expression = "java(com.github.f4b6a3.uuid.UuidCreator.getTimeOrderedEpoch())")
    @Mapping(target = "timestamp", expression = "java(java.time.Instant.now())")
    @Mapping(target = "state", constant = "CREATED")
    Message toMessage(Long senderId, MessageRequest messageRequest);

    MessageDTO toMessageDTO(Message message);
}
