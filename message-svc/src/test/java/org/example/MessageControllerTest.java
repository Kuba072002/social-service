package org.example;

import org.example.application.dto.MessageDTO;
import org.example.application.dto.MessageEditRequest;
import org.example.application.dto.MessageRequest;
import org.example.application.message.MessageMapper;
import org.example.application.message.event.MessageEvent;
import org.example.application.message.event.MessagePublisher;
import org.example.domain.chat.Chat;
import org.example.domain.chat.ChatRepository;
import org.example.domain.chat.Users;
import org.example.domain.message.Message;
import org.example.domain.message.MessageRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import wiremock.org.apache.commons.lang3.RandomUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.example.TestUtils.mockGetUser;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = IntegrationTestInitializer.class)
public class MessageControllerTest {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private MessageMapper messageMapper;
    @MockitoBean
    private MessagePublisher messagePublisher;
    ArgumentCaptor<MessageEvent> eventCaptor = ArgumentCaptor.forClass(MessageEvent.class);

    @Test
    public void shouldCreateMessageWhenRequested() {
        int numberOfParticipants = 3;
        var senderId = RandomUtils.nextLong();
        var chat = createChat(senderId, numberOfParticipants);
        var request = new MessageRequest(chat.getId(), randomAlphabetic(255));

        mockGetUser(senderId);
        HttpHeaders headers = new HttpHeaders();
        headers.set("userId", String.valueOf(senderId));
        var result = restTemplate.postForEntity("/messages",
                new HttpEntity<>(request, headers),
                UUID.class
        );
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assert result.getBody() != null;
        var message = messageRepository.findByChatIdAndMessageId(chat.getId(), result.getBody());
        assert message.isPresent();
        assertThat(message.get().getContent()).isEqualTo(request.content());
        Mockito.verify(messagePublisher, Mockito.times(1)).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue().chatId()).isEqualTo(chat.getId());
        assertThat(eventCaptor.getValue().type()).isEqualTo("POST");
        assertThat(eventCaptor.getValue().userIds()).isEqualTo(chat.getUsers().getIds());
    }

    @Test
    public void shouldEditMessageWhenRequested() {
        int numberOfParticipants = 3;
        var senderId = RandomUtils.nextLong();
        var chat = createChat(senderId, numberOfParticipants);
        var message = createMessage(senderId, chat.getId());
        mockGetUser(senderId);

        var request = new MessageEditRequest(
                chat.getId(),
                message.getMessageId(),
                randomAlphabetic(300)
        );
        HttpHeaders headers = new HttpHeaders();
        headers.set("userId", String.valueOf(senderId));
        ResponseEntity<Void> result = restTemplate.exchange(
                "/messages",
                HttpMethod.PUT,
                new HttpEntity<>(request, headers),
                Void.class);
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        var updatedMessage = messageRepository.findByChatIdAndMessageId(chat.getId(), message.getMessageId());
        assert updatedMessage.isPresent();
        assertThat(updatedMessage.get().getContent()).isEqualTo(request.content());
    }

    @Test
    public void shouldDeleteMessageWhenRequested() {
        int numberOfParticipants = 3;
        var senderId = RandomUtils.nextLong();
        var chat = createChat(senderId, numberOfParticipants);
        var message = createMessage(senderId, chat.getId());
        mockGetUser(senderId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("userId", String.valueOf(senderId));
        ResponseEntity<Void> result = restTemplate.exchange(
                "/messages?chatId=" + chat.getId() + "&messageId=" + message.getMessageId(),
                HttpMethod.DELETE,
                new HttpEntity<>(null, headers),
                Void.class);
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        var updatedMessage = messageRepository.findByChatIdAndMessageId(chat.getId(), message.getMessageId());
        assert updatedMessage.isPresent();
        assertThat(updatedMessage.get().getContent().isBlank()).isTrue();
    }

    @Test
    public void shouldReturnMessagesWhenRequested() {
        int numberOfParticipants = 4;
        int numberOfMessages = 20;
        var senderId = RandomUtils.nextLong();
        var chat = createChat(senderId, numberOfParticipants);
        var messages = createMessages(chat.getUsers().getIds(), chat.getId(), numberOfMessages);
        mockGetUser(senderId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("userId", String.valueOf(senderId));
        ResponseEntity<List<MessageDTO>> result = restTemplate.exchange(
                "/messages?chatId=" + chat.getId() + "&limit=" + numberOfMessages,
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                new ParameterizedTypeReference<>() {
                });
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assert result.getBody() != null;
        assertThat(result.getBody()).hasSize(numberOfMessages);
        assertThat(result.getBody().stream()
                .map(MessageDTO::messageId)
                .toList()
        ).containsExactlyInAnyOrderElementsOf(
                messages.stream().map(Message::getMessageId).toList()
        );
    }

    public Chat createChat(Long userId, int numberOfParticipants) {
        var userIds = IntStream.range(0, numberOfParticipants - 1)
                .mapToObj(i -> RandomUtils.nextLong())
                .collect(Collectors.toSet());
        userIds.add(userId);
        var chat = new Chat(RandomUtils.nextLong(), new Users(userIds));
        chatRepository.save(chat);
        return chat;
    }

    private Message createMessage(long senderId, Long chatId) {
        var request = new MessageRequest(chatId, randomAlphabetic(255));
        var message = messageMapper.toMessage(senderId, request);
        messageRepository.save(message);
        return message;
    }

    private List<Message> createMessages(Set<Long> userIds, Long chatId, int numberOfMessages) {
        var userIdsList = new ArrayList<>(userIds);
        List<Message> messages = new ArrayList<>();
        for (int i = 0; i < numberOfMessages; i++) {
            var request = new MessageRequest(chatId, randomAlphabetic(255));
            var message = messageMapper.toMessage(
                    userIdsList.get(RandomUtils.nextInt(0, userIds.size())),
                    request
            );
            messages.add(message);
        }
        messageRepository.saveAll(messages);
        return messages;
    }
}
