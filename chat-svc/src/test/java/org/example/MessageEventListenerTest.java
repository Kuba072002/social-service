package org.example;

import org.apache.commons.lang3.RandomUtils;
import org.example.application.message.MessageEvent;
import org.example.domain.chat.repository.ChatParticipantRepository;
import org.example.domain.chat.repository.ChatRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.example.TestUtils.createChat;
import static org.example.TestUtils.getRandomUserIds;

class MessageEventListenerTest extends BaseIntegrationTest {

    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private ChatParticipantRepository chatParticipantRepository;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String QUEUE_NAME = "message_events_queue";
    private static final int TIMEOUT = 3;

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldProcessEventWhenReceivedMessageWithPostType(boolean isGreater) {
        var userIds = getRandomUserIds(3);
        var chat = createChat(false, userIds);
        chat.setLastMessageAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        chatRepository.save(chat);
        MessageEvent messageEvent = new MessageEvent("POST", chat.getId(),
                Instant.now().plusSeconds(isGreater ? 10 : -10).truncatedTo(ChronoUnit.MICROS), null, null);

        rabbitTemplate.convertAndSend(QUEUE_NAME, messageEvent);

        await()
                .atMost(TIMEOUT, SECONDS)
                .untilAsserted(() -> {
                    var updatedChat = chatRepository.findById(chat.getId()).orElse(null);
                    assert updatedChat != null;
                    if (isGreater) {
                        assertThat(updatedChat.getLastMessageAt()).isEqualTo(messageEvent.lastMessageCreatedAt());
                    } else {
                        assertThat(updatedChat.getLastMessageAt()).isEqualTo(chat.getLastMessageAt());
                    }
                });
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldProcessEventWhenReceivedMessageWithGetType(boolean isGreater) {
        var userId = RandomUtils.nextLong();
        var userIds = getRandomUserIds(2);
        var currentTime = Instant.now().truncatedTo(ChronoUnit.MICROS);
        userIds.add(userId);
        var chat = createChat(false, userIds);
        if (!isGreater) {
            chat.getParticipants().stream()
                    .filter(cp -> cp.getUserId().equals(userId))
                    .forEach(cp -> cp.setLastReadAt(currentTime));
        }
        chatRepository.save(chat);
        MessageEvent messageEvent = new MessageEvent("GET", chat.getId(), null,
                userId, currentTime.plusSeconds(isGreater ? 10 : -10));

        rabbitTemplate.convertAndSend(QUEUE_NAME, messageEvent);

        await()
                .atMost(TIMEOUT, SECONDS)
                .untilAsserted(() -> {
                    var chatParticipant = chatParticipantRepository.findByChatIdAndUserId(chat.getId(), userId).orElse(null);
                    assert chatParticipant != null;
                    if (isGreater) {
                        assertThat(chatParticipant.getLastReadAt()).isEqualTo(messageEvent.lastReadAt());
                    } else {
                        assertThat(chatParticipant.getLastReadAt()).isEqualTo(currentTime);
                    }
                });
    }
}
