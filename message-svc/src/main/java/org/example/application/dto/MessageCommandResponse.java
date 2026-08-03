package org.example.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MessageCommandResponse(
        UUID messageId,
        UUID clientMessageId
) {
    public static MessageCommandResponse of(UUID messageId) {
        return new MessageCommandResponse(messageId, null);
    }
}
