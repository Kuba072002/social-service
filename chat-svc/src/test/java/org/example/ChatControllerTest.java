package org.example;

import org.example.application.chat.dto.ChatRequest;
import org.example.domain.chat.entity.Chat;
import org.example.domain.chat.entity.ChatParticipant;
import org.example.domain.chat.projection.ChatDetail;
import org.example.domain.chat.repository.ChatParticipantRepository;
import org.example.domain.chat.repository.ChatRepository;
import org.example.domain.event.ChatEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.TestUtils.*;
import static org.example.common.ChatApplicationError.PRIVATE_CHAT_ALREADY_EXISTS;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = IntegrationTestInitializer.class)
class ChatControllerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatControllerTest.class);

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private ChatParticipantRepository chatParticipantRepository;
    @MockitoBean
    private ChatEventPublisher chatEventPublisher;

    @Test
    void shouldCreateChatWhenRequested() {
        int numberOfParticipants = 3;
        var userIds = IntStream.range(0, numberOfParticipants)
                .mapToObj(i -> RandomUtils.nextLong())
                .collect(Collectors.toSet());
        var senderId = RandomUtils.nextLong();
        ChatRequest chatRequest = new ChatRequest(
                randomAlphabetic(12),
                randomAlphabetic(12),
                false,
                userIds
        );
        mockGetUser(senderId);
        mockGetUsers(userIds);

        HttpHeaders headers = new HttpHeaders();
        headers.set("userId", String.valueOf(senderId));
        var result = restTemplate.postForEntity("/chats",
                new HttpEntity<>(chatRequest, headers),
                Long.class
        );
        assert result.getBody() != null;
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        var savedChat = chatRepository.findWithParticipantsById(result.getBody()).orElse(null);
        LOGGER.info("Saved chat: {}", savedChat);
        assertThat(savedChat).isNotNull();
        assertThat(savedChat.getParticipants()).hasSize(numberOfParticipants + 1);
    }

    @Test
    void shouldCreatePrivateChatWhenRequested() {
        var userIds = Set.of(RandomUtils.nextLong());
        var senderId = RandomUtils.nextLong();
        ChatRequest chatRequest = new ChatRequest(null, null, true, userIds);
        mockGetUser(senderId);
        mockGetUsers(userIds);

        HttpHeaders headers = new HttpHeaders();
        headers.set("userId", String.valueOf(senderId));
        var result = restTemplate.postForEntity("/chats",
                new HttpEntity<>(chatRequest, headers),
                Long.class
        );
        assert result.getBody() != null;
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        var savedChat = chatRepository.findWithParticipantsById(result.getBody()).orElse(null);
        LOGGER.info("Saved chat: {}", savedChat);
        assertThat(savedChat).isNotNull();
        assertThat(savedChat.getParticipants()).hasSize(2);
    }

    @Test
    void shouldNotCreatePrivateChatWhenAlreadyExists() {
        var senderId = RandomUtils.nextLong();
        var secondUser = RandomUtils.nextLong();
        var userIds = Set.of(secondUser);
        ChatRequest chatRequest = new ChatRequest(null, null, true, userIds);
        mockGetUser(senderId);
        mockGetUsers(userIds);

        Chat chat = createChat(true, List.of(senderId, secondUser));
        chatRepository.save(chat);

        HttpHeaders headers = new HttpHeaders();
        headers.set("userId", String.valueOf(senderId));
        var result = restTemplate.postForEntity("/chats", new HttpEntity<>(chatRequest, headers), ServiceResponse.class);
        assert result.getBody() != null;
        assertThat(result.getStatusCode()).isEqualTo(PRIVATE_CHAT_ALREADY_EXISTS.getStatus());
        assertThat(result.getBody().message()).isEqualTo(PRIVATE_CHAT_ALREADY_EXISTS.getMessage());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnUserChatsWhenRequested(boolean isPrivate) {
        int numberOfGroupChats = 4;
        int numberOfPrivateChats = 5;
        Long senderId = RandomUtils.nextLong();
        mockGetUser(senderId);
        var groupChats = IntStream.range(0, numberOfGroupChats)
                .mapToObj(i -> {
                    var participantIds = IntStream.range(0, RandomUtils.nextInt(0, 5))
                            .mapToObj(j -> RandomUtils.nextLong())
                            .collect(Collectors.toList());
                    participantIds.add(senderId);
                    return createChat(false, participantIds);
                }).toList();
        chatRepository.saveAll(groupChats);
        var privateChats = IntStream.range(0, numberOfPrivateChats)
                .mapToObj(i -> createChat(true, List.of(senderId, RandomUtils.nextLong())))
                .toList();
        chatRepository.saveAll(privateChats);
        if (isPrivate) {
            var userIdsToFetch = privateChats.stream()
                    .map(Chat::getParticipants)
                    .flatMap(Collection::stream)
                    .map(ChatParticipant::getUserId)
                    .filter(cp -> !cp.equals(senderId))
                    .collect(Collectors.toSet());
            mockGetUsers(userIdsToFetch);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("userId", String.valueOf(senderId));
        ResponseEntity<List<ChatDetail>> result = restTemplate.exchange("/chats?isPrivate=" + isPrivate,
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                new ParameterizedTypeReference<>() {
                });

        var expectedChatsInResponse = isPrivate ? privateChats : groupChats;

        assert result.getBody() != null;
        var chatDetails = result.getBody();
        LOGGER.info("ChatDetails: {}", chatDetails);
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(chatDetails).hasSize(expectedChatsInResponse.size());

        var chatMap = expectedChatsInResponse.stream()
                .collect(Collectors.toMap(Chat::getId, Function.identity()));

        for (ChatDetail chatDetail : chatDetails) {
            assertThat(chatDetail.getIsPrivate()).isEqualTo(isPrivate);
            assert chatDetail.getName() != null;
            if (isPrivate) {
                assert chatDetail.getOtherUser() != null;
                var otherUserId = chatMap.get(chatDetail.getChatId())
                        .getParticipants()
                        .stream()
                        .map(ChatParticipant::getUserId)
                        .filter(userId -> !userId.equals(senderId))
                        .findFirst()
                        .orElse(null);
                assertThat(chatDetail.getOtherUser()).isEqualTo(otherUserId);
            } else {
                assert chatDetail.getOtherUser() == null;
                var name = chatMap.get(chatDetail.getChatId()).getName();
                assertThat(chatDetail.getName()).isEqualTo(name);
            }
        }
    }

    @Test
    void shouldReturnChatParticipantsWhenRequested() {
        int numberOfParticipants = 3;
        Long senderId = RandomUtils.nextLong();
        mockGetUser(senderId);
        var userIds = IntStream.range(0, RandomUtils.nextInt(0, numberOfParticipants))
                .mapToObj(j -> RandomUtils.nextLong())
                .collect(Collectors.toSet());
        userIds.add(senderId);
        Chat chat = createChat(false, userIds);
        chatRepository.save(chat);

        ResponseEntity<Set<Long>> result = restTemplate.exchange(
                "/internal/chats/participants/ids?chatId=" + chat.getId(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });
        assert result.getBody() != null;
        var fetchedUserIds = result.getBody();
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();


        assertThat(fetchedUserIds).containsExactlyInAnyOrderElementsOf(userIds);
    }

    private static Chat createChat(boolean isPrivate) {
        var builder = Chat.builder().isPrivate(isPrivate);
        if (isPrivate) {
            return builder.build();
        } else {
            return builder
                    .name(randomAlphabetic(12))
                    .imageUrl(randomAlphabetic(12))
                    .build();
        }
    }

    private static Chat createChat(boolean isPrivate, Collection<Long> userIds) {
        Chat chat = createChat(isPrivate);
        chat.setParticipants(userIds.stream()
                .map(userId -> new ChatParticipant(chat, userId))
                .toList());
        return chat;
    }

}
