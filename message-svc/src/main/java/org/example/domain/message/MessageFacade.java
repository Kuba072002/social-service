package org.example.domain.message;

import lombok.RequiredArgsConstructor;
import org.example.domain.message.event.MessagePublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class MessageFacade {
    private final MessageRepository messageRepository;
    private final MessagePublisher messagePublisher;
    private final StorageService storageService;

    public void createMessage(Set<Long> chatParticipantIds, Message message, MultipartFile multipartFile) {
        if (multipartFile != null) {
            var fileUrl = storageService.storeFile(multipartFile);
            message.setMediaContent(fileUrl);
        }
        messageRepository.save(message);
        messagePublisher.publish(chatParticipantIds, message);
    }

    public List<Message> getMessages(Long userId, Long chatId, Instant from, Instant to, Integer limit) {
        var messages = messageRepository.findAllByChatIdAndCreatedAtBetween(chatId, from, to, limit);
        if (!messages.isEmpty()) {
            messagePublisher.publish(userId, chatId, messages.get(0).getCreatedAt());
        }
        return messages;
    }

}
