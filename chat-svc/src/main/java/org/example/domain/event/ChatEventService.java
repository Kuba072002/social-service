package org.example.domain.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatEventService {
    private final ChatEventOutboxRepository outboxRepository;

    public void save(ChatEvent chatEvent) {
        outboxRepository.save(new ChatEventOutbox(chatEvent));
    }

    public List<ChatEventOutbox> findEventsBatch(int batchSize) {
        return outboxRepository.findBatch(PageRequest.of(0, batchSize));
    }

    public void deleteEvents(List<ChatEventOutbox> events) {
        outboxRepository.deleteAll(events);
    }
}
