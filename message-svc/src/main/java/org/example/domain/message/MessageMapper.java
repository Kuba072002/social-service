package org.example.domain.message;

import org.example.application.MessageDTO;
import org.mapstruct.Mapper;

@Mapper
public interface MessageMapper {
    Message toMessage(Long senderId, MessageDTO messageDTO);
}
