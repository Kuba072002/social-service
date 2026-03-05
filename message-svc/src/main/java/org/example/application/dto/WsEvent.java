package org.example.application.dto;

import org.example.domain.message.Message;

public record WsEvent<T>(
        String type,
        T payload
) {
    private static final String MESSAGE_EVENT = "MESSAGE";
    private static final String MESSAGE_READ_EVENT = "MESSAGE_READ";

    public static WsEvent<Message> of(Message message) {
        return new WsEvent<>(MESSAGE_EVENT, message);
    }

    public static WsEvent<ChatActivityDTO> of(ChatActivityDTO chatActivityDTO) {
        return new WsEvent<>(MESSAGE_READ_EVENT, chatActivityDTO);
    }
}
