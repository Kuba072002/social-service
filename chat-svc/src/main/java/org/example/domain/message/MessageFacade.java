package org.example.domain.message;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class MessageFacade {
    private final MessageService messageService;

    public List<LatestChatMessagesDTO> getLatestChatMessages(@NonNull Set<Long> chatIds) {
        if (chatIds.isEmpty()) {
            return Collections.emptyList();
        }
        return messageService.getLatestChatMessages(chatIds);
    }
}
