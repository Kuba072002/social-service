package org.example;

import org.apache.commons.lang3.RandomUtils;
import org.example.application.chat.dto.*;
import org.example.domain.chat.entity.Chat;
import org.example.domain.chat.entity.ChatParticipant;
import org.example.domain.chat.projection.ChatDetail;
import org.example.domain.chat.repository.ChatParticipantRepository;
import org.example.domain.chat.repository.ChatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.TestUtils.*;
import static org.example.common.ChatApplicationError.PRIVATE_CHAT_ALREADY_EXISTS;

class ChatControllerTest extends BaseIntegrationTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatControllerTest.class);

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private ChatParticipantRepository chatParticipantRepository;

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldCreateChatWhenRequested(boolean isPrivate) {
        int numberOfParticipants = isPrivate ? 1 : 3;
        var userIds = getRandomUserIds(numberOfParticipants);
        var senderId = RandomUtils.secure().randomLong();
        if (isPrivate) { // for test existsPrivateChat query
            var participantIds = new HashSet<>(userIds);
            participantIds.add(senderId);
            createChat(false, participantIds);
        }
        ChatRequest chatRequest = isPrivate
                ? new ChatRequest(null, null, true, userIds)
                : new ChatRequest(randomAlphabetic(12), randomAlphabetic(12), false, userIds);

        mockGetUser(senderId);
        mockGetUsers(userIds);
        var result = restTemplate.postForEntity("/chats", new HttpEntity<>(chatRequest, getHttpHeaders(senderId)), Long.class);

        assert result.getBody() != null;
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        var savedChat = chatRepository.findWithParticipantsById(result.getBody()).orElse(null);
        LOGGER.info("Saved chat: {}", savedChat);
        assertThat(savedChat).isNotNull();
        assertThat(savedChat.getParticipants()).hasSize(numberOfParticipants + 1);
    }

    @Test
    void shouldNotCreatePrivateChatWhenAlreadyExists() {
        var senderId = RandomUtils.secure().randomLong();
        var secondUser = RandomUtils.secure().randomLong();
        var userIds = Set.of(secondUser);
        ChatRequest chatRequest = new ChatRequest(null, null, true, userIds);
        Chat chat = createChat(true, List.of(senderId, secondUser));
        chatRepository.save(chat);

        mockGetUser(senderId);
        mockGetUsers(userIds);
        var result = restTemplate.postForEntity("/chats", new HttpEntity<>(chatRequest, getHttpHeaders(senderId)), ServiceResponse.class);

        assert result.getBody() != null;
        assertThat(result.getStatusCode()).isEqualTo(PRIVATE_CHAT_ALREADY_EXISTS.getStatus());
        assertThat(result.getBody().message()).isEqualTo(PRIVATE_CHAT_ALREADY_EXISTS.getMessage());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnUserChatsWhenRequested(boolean isPrivate) {
        int numberOfGroupChats = 4;
        int numberOfPrivateChats = 5;
        Long senderId = RandomUtils.secure().randomLong();
        var groupChats = IntStream.range(0, numberOfGroupChats)
                .mapToObj(i -> {
                    var participantIds = IntStream.range(0, RandomUtils.secure().randomInt(0, 5))
                            .mapToObj(j -> RandomUtils.secure().randomLong())
                            .collect(Collectors.toList());
                    participantIds.add(senderId);
                    return createChat(false, participantIds);
                }).toList();
        chatRepository.saveAll(groupChats);
        var privateChats = IntStream.range(0, numberOfPrivateChats)
                .mapToObj(i -> createChat(true, List.of(senderId, RandomUtils.secure().randomLong())))
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

        mockGetUser(senderId);
        ResponseEntity<List<ChatDetail>> result = restTemplate.exchange("/chats?isPrivate=" + isPrivate,
                HttpMethod.GET,
                new HttpEntity<>(null, getHttpHeaders(senderId)),
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
        Long senderId = RandomUtils.secure().randomLong();
        var userIds = getRandomUserIds(numberOfParticipants);
        userIds.add(senderId);
        Chat chat = createChat(false, userIds);
        chatRepository.save(chat);

        mockGetUser(senderId);
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

    @Test
    void shouldModifyChatWhenRequested() {
        int numberOfParticipants = 2;
        Long senderId = RandomUtils.secure().randomLong();
        var userIds = getRandomUserIds(numberOfParticipants);
        Chat chat = createChat(false, userIds, senderId);
        chatRepository.save(chat);

        var request = new ModifyChatRequest(randomAlphabetic(10), randomAlphabetic(10));
        mockGetUser(senderId);
        ResponseEntity<Void> result = restTemplate.exchange(
                "/chats/" + chat.getId(), HttpMethod.PUT, new HttpEntity<>(request, getHttpHeaders(senderId)), Void.class);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        var savedChat = chatRepository.findWithParticipantsById(chat.getId()).orElse(null);
        assertThat(savedChat).isNotNull();
        assertThat(savedChat.getName()).isEqualTo(request.name());
        assertThat(savedChat.getImageUrl()).isEqualTo(request.imageUrl());
    }

    @Test
    void shouldModifyChatParticipantsWhenRequested() {
        int numberOfParticipants = 6, numberOfParticipantsToAdd = 2, numberOfParticipantsToRemove = numberOfParticipants - 1;
        Long senderId = RandomUtils.secure().randomLong();
        var userIds = getRandomUserIds(numberOfParticipants);
        Chat chat = createChat(false, userIds, senderId);
        chatRepository.save(chat);

        var request = new ModifyChatParticipantsRequest(
                getRandomUserIds(numberOfParticipantsToAdd),
                userIds.stream().limit(numberOfParticipantsToRemove).collect(Collectors.toSet())
        );
        mockGetUser(senderId);
        mockGetUsers(request.userIdsToAdd());
        ResponseEntity<Void> result = restTemplate.exchange(
                "/chats/" + chat.getId() + "/participants", HttpMethod.PUT, new HttpEntity<>(request, getHttpHeaders(senderId)), Void.class);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        var savedChat = chatRepository.findWithParticipantsById(chat.getId()).orElse(null);
        assertThat(savedChat).isNotNull();
        assertThat(savedChat.getParticipants()).hasSize(numberOfParticipants - numberOfParticipantsToRemove + numberOfParticipantsToAdd + 1);
        var savedParticipants = savedChat.getParticipants().stream().map(ChatParticipant::getUserId).collect(Collectors.toSet());
        var userIdsToStay = new HashSet<>(userIds);
        userIdsToStay.removeAll(request.userIdsToDelete());
        assertThat(savedParticipants)
                .containsAll(request.userIdsToAdd())
                .doesNotContainAnyElementsOf(request.userIdsToDelete())
                .containsAll(userIdsToStay);
    }

    @Test
    void shouldDeleteChatParticipantWhenRequested() {
        int numberOfParticipants = 2;
        Long senderId = RandomUtils.secure().randomLong();
        var userIds = getRandomUserIds(numberOfParticipants);
        Chat chat = createChat(false, userIds, senderId);
        chatRepository.save(chat);

        mockGetUser(senderId);
        ResponseEntity<Void> result = restTemplate.exchange(
                "/chats/" + chat.getId() + "/participants", HttpMethod.DELETE, new HttpEntity<>(null, getHttpHeaders(senderId)), Void.class);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        var savedChat = chatRepository.findWithParticipantsById(chat.getId()).orElse(null);
        assertThat(savedChat).isNotNull();
        var participants = savedChat.getParticipants().stream().map(ChatParticipant::getUserId).collect(Collectors.toSet());
        assertThat(participants).containsExactlyInAnyOrderElementsOf(userIds);
    }

    @Test
    void shouldGetChatParticipantsWhenRequested() {
        int numberOfParticipants = 4;
        Long senderId = RandomUtils.secure().randomLong();
        var userIds = getRandomUserIds(numberOfParticipants);
        Chat chat = createChat(false, userIds, senderId);
        chatRepository.save(chat);

        userIds.add(senderId);
        mockGetUser(senderId);
        mockGetUsers(userIds);
        ResponseEntity<List<ParticipantDTO>> result = restTemplate.exchange(
                "/chats/" + chat.getId() + "/participants",
                HttpMethod.GET, new HttpEntity<>(null, getHttpHeaders(senderId)),
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody()).isNotNull()
                .hasSize(numberOfParticipants + 1)
                .extracting(ParticipantDTO::userId)
                .containsExactlyInAnyOrderElementsOf(userIds);
    }

    @Test
    void shouldDeleteChatWhenRequested() {
        int numberOfParticipants = 2;
        Long senderId = RandomUtils.secure().randomLong();
        var userIds = getRandomUserIds(numberOfParticipants);
        Chat chat = createChat(false, userIds, senderId);
        chatRepository.save(chat);

        mockGetUser(senderId);
        ResponseEntity<Void> result = restTemplate.exchange(
                "/chats/" + chat.getId(), HttpMethod.DELETE, new HttpEntity<>(null, getHttpHeaders(senderId)), Void.class);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        var savedChat = chatRepository.findWithParticipantsById(chat.getId()).orElse(null);
        assertThat(savedChat).isNull();
    }

    @Test
    void shouldUpdateLastReadAtChatWhenRequested() {
        int numberOfParticipants = 2;
        Long senderId = RandomUtils.secure().randomLong();
        var userIds = getRandomUserIds(numberOfParticipants);
        Chat chat = createChat(false, userIds, senderId);
        chatRepository.save(chat);

        mockGetUser(senderId);
        var request = new UpdateChatReadAtRequest(Instant.now().truncatedTo(ChronoUnit.MICROS));
        ResponseEntity<Void> result = restTemplate.exchange(
                "/chats/" + chat.getId() + "/participants/last_read_at", HttpMethod.PUT,
                new HttpEntity<>(request, getHttpHeaders(senderId)), Void.class
        );

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        var savedChat = chatRepository.findWithParticipantsById(chat.getId()).orElse(null);
        assertThat(savedChat).isNotNull();
        var lastReadAt = savedChat.getParticipants().stream()
                .filter(cp -> cp.getUserId().equals(senderId))
                .map(ChatParticipant::getLastReadAt)
                .findFirst()
                .orElse(null);
        assertThat(lastReadAt).isEqualTo(request.lastReadAt());
    }

    private static HttpHeaders getHttpHeaders(Long senderId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("userId", String.valueOf(senderId));
        return headers;
    }

}
