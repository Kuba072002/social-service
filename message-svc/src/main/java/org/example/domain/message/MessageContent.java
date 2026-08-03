package org.example.domain.message;

public record MessageContent(
        MessageContentType type,
        String value
) {
    public static MessageContent text(String content) {
        return new MessageContent(MessageContentType.TEXT, content);
    }
}
