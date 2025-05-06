package org.example.domain.message;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MessageFacade {
    private final MessageRepository messageRepository;
    private final StorageService storageService;

    @Transactional
    public void createMessage(Message message, MultipartFile multipartFile) {
        if (multipartFile != null) {
            var fileUrl = storageService.storeFile(multipartFile);
            message.setMediaContent(fileUrl);
        }
        messageRepository.save(message);
    }

    public List<Message> getMessages(Long chatId, Instant from, Instant to, Integer limit) {
        return messageRepository.findAllByChatIdAndCreatedAtBetween(chatId, from, to, limit);
    }

}
