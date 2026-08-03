package org.example.application.message.service;

import org.example.domain.message.Message;
import org.example.domain.message.MessageContent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface MessageMapper {

    @Mapping(target = "messageId", expression = "java(com.github.f4b6a3.uuid.UuidCreator.getTimeOrderedEpoch())")
    @Mapping(target = "content", expression = "java(toMessageContent(content))")
    @Mapping(target = "timestamp", expression = "java(java.time.Instant.now())")
    @Mapping(target = "state", constant = "CREATED")
    Message toMessage(Long senderId, Long chatId, String content);

    default MessageContent toMessageContent(String content) {
        return MessageContent.text(content);
    }
}
