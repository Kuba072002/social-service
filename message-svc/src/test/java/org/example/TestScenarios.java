package org.example;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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
import wiremock.org.apache.commons.lang3.RandomUtils;

import javax.crypto.SecretKey;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.TestUtils.mockGetUser;
import static org.example.TestUtils.randomAlphabetic;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = IntegrationTestInitializer.class)
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
    //    @Autowired
//    private SimpUserRegistry simpUserRegistry;
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
        var senderId = RandomUtils.nextLong();
        CompletableFuture<String> messageFuture = new CompletableFuture<>();

        connectUserByStomp(senderId, messageFuture);
        LOGGER.info("Sending message to user: {}", senderId);
        messagingTemplate.convertAndSendToUser(String.valueOf(senderId), "/queue/messages", "Test message.");

        String message = messageFuture.get(4, TimeUnit.SECONDS);
        assertThat(message).isEqualTo("Test message.");
    }

    @Test
    void shouldCreateMessageWhenRequested() throws Exception {
        var senderId = RandomUtils.nextLong();
        var chatId = RandomUtils.nextLong();
        MessageRequest messageRequest = new MessageRequest(chatId, randomAlphabetic(50));
        var connectedUserId = RandomUtils.nextLong();
        Set<Long> participants = Set.of(connectedUserId, RandomUtils.nextLong(), senderId);
        putParticipantsToCache(chatId, participants);
        CompletableFuture<String> messageFuture = new CompletableFuture<>();
        connectUserByStomp(connectedUserId, messageFuture);

        mockGetUser(senderId);
        var result = restTemplate.postForEntity("/messages", new HttpEntity<>(messageRequest, getHttpHeaders(senderId)), UUID.class);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assert result.getBody() != null;
        assertThat(messageRepository.findByChatIdAndMessageId(chatId, result.getBody())).isNotNull();
        String receivedMessage = messageFuture.get(4, TimeUnit.SECONDS);
        LOGGER.info("Client got: {}", receivedMessage);
        assertThat(receivedMessage).isNotNull();
        Message message = Utils.readJson(receivedMessage, Message.class);
        assert message != null;
        assertThat(message.getContent()).isEqualTo(messageRequest.content());
    }

    private void putParticipantsToCache(long chatId, Set<Long> participants) {
        Cache cache = cacheManager.getCache("chatParticipantIds");
        assertThat(cache).isNotNull();
        cache.put(chatId, participants);
    }

    private static HttpHeaders getHttpHeaders(Long senderId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("userId", String.valueOf(senderId));
        return headers;
    }

    private void connectUserByStomp(Long participantId, CompletableFuture<String> messageFuture) throws Exception {
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
            public Type getPayloadType(@NotNull StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(@NotNull StompHeaders headers, Object payload) {
                LOGGER.info("handleFrame() >> {}", payload);
                messageFuture.complete((String) payload);
            }
        });

        Thread.sleep(300);
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
