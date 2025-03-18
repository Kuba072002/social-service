package org.example.application.chat.dto;

import jakarta.validation.constraints.*;

import java.util.Set;

public record ChatRequest(
        @NotBlank(groups = GroupChatGroup.class)
        @Null(groups = PrivateChatGroup.class)
        String name,
        String imageUrl,
        Boolean isPrivate,
        @NotEmpty
        @Size(max = 20, groups = GroupChatGroup.class)
        @Size(max = 1, groups = PrivateChatGroup.class)
        Set<Long> userIds
) {
    public interface PrivateChatGroup {
    }

    public interface GroupChatGroup {
    }
}
