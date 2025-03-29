package org.example.domain.message;

import lombok.RequiredArgsConstructor;
import org.example.application.dto.ChatDTO;
import org.example.domain.chat.ChatFacade;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MessageFacade {
    private final ChatFacade chatFacade;
    private final MessageRepository messageRepository;

    public Set<Long> createMessage(Message message) {
        var chatDTO = chatFacade.getChat(message.getChatId());
        validateRequest(chatDTO, message.getSenderId());
        messageRepository.save(message);

        return chatDTO.participantIds().stream()
                .filter(p -> !p.equals(message.getSenderId()))
                .collect(Collectors.toSet());
    }

    private void validateRequest(ChatDTO chatDTO, Long senderId) {
        chatDTO.participantIds().stream()
                .filter(p -> p.equals(senderId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Sender not involved in chat"));
    }
}
