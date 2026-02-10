package org.example;

import com.github.f4b6a3.uuid.UuidCreator;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.lang3.RandomUtils;
import org.example.application.dto.MessageDTO;
import org.example.application.dto.MessageEditRequest;
import org.example.application.dto.MessageRequest;
import org.example.common.Utils;
import org.example.domain.message.Message;
import org.example.domain.message.MessageRepository;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import javax.crypto.SecretKey;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.TestUtils.randomAlphabetic;
import static org.example.common.Constants.USER_ID_HEADER;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = IntegrationTestInitializer.class)
@AutoConfigureTestRestTemplate
class TestScenarios {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestScenarios.class);

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;

    private final SecretKey key = Keys.hmacShaKeyFor("long_and_secure_jwt_secret_for_development".getBytes(StandardCharsets.UTF_8));

    @BeforeEach
    void setup() {
        List<Transport> transports = List.of(new WebSocketTransport(new StandardWebSocketClient()));
        SockJsClient sockJsClient = new SockJsClient(transports);
        stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new StringMessageConverter());

        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.initialize();
        stompClient.setTaskScheduler(taskScheduler);
        stompClient.setDefaultHeartbeat(new long[]{10000, 10000});
    }

    @Test
    void shouldConnectViaStomp() throws Exception {
        var senderId = RandomUtils.secure().randomLong();
        CompletableFuture<String> messageFuture = new CompletableFuture<>();

        StompSession session = connectUserByStomp(senderId, messageFuture);
        LOGGER.info("Sending message to user: {}", senderId);
        messagingTemplate.convertAndSendToUser(String.valueOf(senderId), "/queue/messages", "Test message.");

        String message = messageFuture.get(4, TimeUnit.SECONDS);
        assertThat(message).isEqualTo("Test message.");
        session.disconnect();
    }

    @Test
    void shouldCreateMessageWhenRequested() throws Exception {
        var senderId = RandomUtils.secure().randomLong();
        var chatId = RandomUtils.secure().randomLong();
        MessageRequest messageRequest = new MessageRequest(chatId, randomAlphabetic(50));
        var connectedUserId = RandomUtils.secure().randomLong();
        Set<Long> participants = Set.of(connectedUserId, RandomUtils.secure().randomLong(), senderId);
        putParticipantsToCache(chatId, participants);
        CompletableFuture<String> messageFuture = new CompletableFuture<>();
        StompSession session = connectUserByStomp(connectedUserId, messageFuture);

        var result = restTemplate.postForEntity("/messages", new HttpEntity<>(messageRequest, getHttpHeaders(senderId)), UUID.class);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assert result.getBody() != null;
        assertThat(messageRepository.findByChatIdAndMessageId(chatId, result.getBody())).isPresent();
        String receivedMessage = messageFuture.get(4, TimeUnit.SECONDS);
        LOGGER.info("Client got: {}", receivedMessage);
        assertThat(receivedMessage).isNotNull();
        Message message = Utils.readJson(receivedMessage, Message.class);
        assert message != null;
        assertThat(message.getContent()).isEqualTo(messageRequest.content());
        session.disconnect();
    }

    @Test
    void shouldEditMessageWhenRequested() throws Exception {
        var senderId = RandomUtils.secure().randomLong();
        var chatId = RandomUtils.secure().randomLong();
        Message orginalMessage = createMessage(chatId, senderId);
        messageRepository.save(orginalMessage);
        MessageEditRequest messageEditRequest = new MessageEditRequest(chatId, orginalMessage.getMessageId(), randomAlphabetic(50));
        var connectedUserId = RandomUtils.secure().randomLong();
        Set<Long> participants = Set.of(RandomUtils.secure().randomLong(), connectedUserId, senderId);
        putParticipantsToCache(chatId, participants);
        CompletableFuture<String> messageFuture = new CompletableFuture<>();
        StompSession session = connectUserByStomp(connectedUserId, messageFuture);

        var result = restTemplate.exchange("/messages",
                HttpMethod.PUT,
                new HttpEntity<>(messageEditRequest, getHttpHeaders(senderId)),
                Void.class);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        var updatedMessage = messageRepository.findByChatIdAndMessageId(chatId, orginalMessage.getMessageId()).orElse(null);
        assertThat(updatedMessage).isNotNull();
        assertThat(updatedMessage.getContent()).isEqualTo(messageEditRequest.content());
        assertThat(updatedMessage.getCreatedAt()).isAfter(orginalMessage.getCreatedAt());

        String receivedMessage = messageFuture.get(4, TimeUnit.SECONDS);
        LOGGER.info("Client got: {}", receivedMessage);
        assertThat(receivedMessage).isNotNull();
        Message message = Utils.readJson(receivedMessage, Message.class);
        assert message != null;
        assertThat(message.getMessageId()).isEqualTo(orginalMessage.getMessageId());
        assertThat(message.getContent()).isEqualTo(messageEditRequest.content());
        session.disconnect();
    }

    @Test
    void shouldDeleteMessageWhenRequested() throws Exception {
        var senderId = RandomUtils.secure().randomLong();
        var chatId = RandomUtils.secure().randomLong();
        Message orginalMessage = createMessage(chatId, senderId);
        messageRepository.save(orginalMessage);
        var connectedUserId = RandomUtils.secure().randomLong();
        Set<Long> participants = Set.of(RandomUtils.secure().randomLong(), connectedUserId, senderId);
        putParticipantsToCache(chatId, participants);
        CompletableFuture<String> messageFuture = new CompletableFuture<>();
        StompSession session = connectUserByStomp(connectedUserId, messageFuture);

        var result = restTemplate.exchange("/messages?chatId=" + chatId + "&messageId=" + orginalMessage.getMessageId(),
                HttpMethod.DELETE,
                new HttpEntity<>(null, getHttpHeaders(senderId)),
                Void.class);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        var updatedMessage = messageRepository.findByChatIdAndMessageId(chatId, orginalMessage.getMessageId()).orElse(null);
        assertThat(updatedMessage).isNotNull();
        assertThat(updatedMessage.isDeleted()).isTrue();
        assertThat(updatedMessage.getCreatedAt()).isAfter(orginalMessage.getCreatedAt());

        String receivedMessage = messageFuture.get(4, TimeUnit.SECONDS);
        LOGGER.info("Client got: {}", receivedMessage);
        assertThat(receivedMessage).isNotNull();
        Message message = Utils.readJson(receivedMessage, Message.class);
        assert message != null;
        assertThat(message.getMessageId()).isEqualTo(orginalMessage.getMessageId());
        assertThat(message.isDeleted()).isTrue();
        session.disconnect();
    }

    @Test
    void shouldGetMessageWhenRequested() {
        var senderId = RandomUtils.secure().randomLong();
        var chatId = RandomUtils.secure().randomLong();
        var otherUserId = RandomUtils.secure().randomLong();
        List<Message> originalMessages = IntStream.range(0, 12)
                .mapToObj(i -> createMessage(chatId, i % 3 == 0 ? senderId : otherUserId))
                .toList();
        messageRepository.saveAll(originalMessages);
        putParticipantsToCache(chatId, Set.of(otherUserId, senderId));

        ResponseEntity<List<MessageDTO>> result = restTemplate.exchange("/messages?chatId=" + chatId,
                HttpMethod.GET,
                new HttpEntity<>(null, getHttpHeaders(senderId)),
                new ParameterizedTypeReference<>() {
                });

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assert result.getBody() != null;
        assertThat(result.getBody()).hasSize(originalMessages.size());
        assertThat(result.getBody().stream().map(MessageDTO::messageId).toList())
                .containsExactlyInAnyOrderElementsOf(originalMessages.stream().map(Message::getMessageId).toList());
    }

    private void putParticipantsToCache(long chatId, Set<Long> participants) {
        Cache cache = cacheManager.getCache("chatParticipantIds");
        assertThat(cache).isNotNull();
        cache.put(chatId, participants);
    }

    private static HttpHeaders getHttpHeaders(Long senderId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(USER_ID_HEADER, String.valueOf(senderId));
        return headers;
    }

    private StompSession connectUserByStomp(Long participantId, CompletableFuture<String> messageFuture) throws Exception {
        String url = "ws://localhost:" + port + "/message-svc/ws";
        StompHeaders headers = new StompHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + generateToken(participantId));
        StompSession session = stompClient
                .connectAsync(new URI(url), null, headers, new StompSessionHandlerAdapter() {
                })
                .get(10, TimeUnit.SECONDS);
        assertThat(session.isConnected()).isTrue();

        session.subscribe("/user/queue/messages", new StompFrameHandler() {
            @Override
            public @NotNull Type getPayloadType(@NotNull StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(@NotNull StompHeaders headers, Object payload) {
                LOGGER.info("handleFrame() >> {}", payload);
                messageFuture.complete((String) payload);
            }
        });

        Thread.sleep(300);
        return session;
    }

    private static Message createMessage(long chatId, long senderId) {
        return Message.builder()
                .messageId(UuidCreator.getTimeOrderedEpoch(Instant.now()))
                .chatId(chatId)
                .senderId(senderId)
                .content(randomAlphabetic(20))
                .createdAt(Instant.now())
                .build();
    }

    private String generateToken(Long userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("userName", "user_name_" + userId)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + 3600000))
                .signWith(key)
                .compact();
    }
}
