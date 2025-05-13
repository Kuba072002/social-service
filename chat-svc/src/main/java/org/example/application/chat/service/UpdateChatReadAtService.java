package org.example.application.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.ApplicationException;
import org.example.application.chat.dto.UpdateChatReadAtRequest;
import org.example.domain.chat.ChatFacade;
import org.springframework.stereotype.Service;

import static org.example.common.ChatApplicationError.USER_DOES_NOT_BELONG_TO_CHAT;

@Service
@RequiredArgsConstructor
public class UpdateChatReadAtService {
    private final ChatFacade chatFacade;

    public void update(Long userId, UpdateChatReadAtRequest updateRequest) {
        var chatParticipant = chatFacade.getChatParticipant(updateRequest.chatId(), userId)
                .orElseThrow(() -> new ApplicationException(USER_DOES_NOT_BELONG_TO_CHAT));
        if (chatParticipant.getLastReadAt() == null
                || updateRequest.lastReadAt().isAfter(chatParticipant.getLastReadAt())) {
            chatParticipant.setLastReadAt(updateRequest.lastReadAt());
            chatFacade.save(chatParticipant);
        }
    }
}
