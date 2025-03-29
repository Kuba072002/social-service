package org.example.application.chat.dto;

import java.util.List;

public record ChatsResponse(
        List<ChatDTO> chats
) {
}
