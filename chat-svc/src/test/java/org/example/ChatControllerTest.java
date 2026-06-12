package org.example;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.RandomUtils;
import org.example.application.chat.dto.ChatParticipantRoleDTO;
import org.example.application.chat.dto.ChatRequest;
import org.example.application.chat.dto.ChatTypeDTO;
import org.example.application.chat.dto.ModifyChatParticipantRole;
import org.example.application.chat.dto.ModifyChatParticipantsRequest;
import org.example.application.chat.dto.ModifyChatRequest;
import org.example.application.chat.dto.ParticipantDTO;
import org.example.application.chat.dto.UpdateChatReadAtRequest;
import org.example.domain.chat.entity.Chat;
import org.example.domain.chat.entity.ChatParticipant;
import org.example.domain.chat.entity.ChatParticipantRole;
import org.example.domain.chat.entity.ChatType;
import org.example.domain.chat.projection.ChatDetail;
import org.example.domain.chat.repository.ChatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
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
import static org.example.TestUtils.createChat;
import static org.example.TestUtils.getRandomUserIds;
import static org.example.TestUtils.mockGetUsers;
import static org.example.TestUtils.randomAlphabetic;
import static org.example.common.ChatApplicationError.PRIVATE_CHAT_ALREADY_EXISTS;
import static org.example.common.Constants.USER_ID_HEADER;

class ChatControllerTest extends BaseIntegrationTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatControllerTest.class);

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private ChatRepository chatRepository;

    @ParameterizedTest
    @EnumSource(names = {"PRIVATE", "GROUP"})
    void shouldCreateChatWhenRequested(ChatTypeDTO chatType) {
        int numberOfParticipants = chatType == ChatTypeDTO.PRIVATE ? 1 : 3;
        var userIds = getRandomUserIds(numberOfParticipants);
        var senderId = RandomUtils.secure().randomLong();
        if (chatType == ChatTypeDTO.PRIVATE) { // for test existsPrivateChat query
            var participantIds = new HashSet<>(userIds);
            participantIds.add(senderId);
            createChat(false, participantIds);
        }
        ChatRequest chatRequest = chatType == ChatTypeDTO.PRIVATE
                ? new ChatRequest(null, null, ChatTypeDTO.PRIVATE, userIds)
                : new ChatRequest(randomAlphabetic(12), randomAlphabetic(12), ChatTypeDTO.GROUP, userIds);

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
        ChatRequest chatRequest = new ChatRequest(null, null, ChatTypeDTO.PRIVATE, userIds);
        Chat chat = createChat(true, List.of(senderId, secondUser));
        chatRepository.save(chat);

        mockGetUsers(userIds);
        var result = restTemplate.postForEntity("/chats", new HttpEntity<>(chatRequest, getHttpHeaders(senderId)), ServiceResponse.class);

        assert result.getBody() != null;
        assertThat(result.getStatusCode()).isEqualTo(PRIVATE_CHAT_ALREADY_EXISTS.getStatus());
        assertThat(result.getBody().message()).isEqualTo(PRIVATE_CHAT_ALREADY_EXISTS.getMessage());
    }

    @ParameterizedTest
    @EnumSource(names = {"PRIVATE", "GROUP"})
    @NullSource
    void shouldReturnUserChatsWhenRequested(ChatTypeDTO chatType) {
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
        if (chatType != ChatTypeDTO.GROUP) {
            var userIdsToFetch = privateChats.stream()
                    .map(Chat::getParticipants)
                    .flatMap(Collection::stream)
                    .map(ChatParticipant::getUserId)
                    .filter(cp -> !cp.equals(senderId))
                    .collect(Collectors.toSet());
            mockGetUsers(userIdsToFetch);
        }

        String url = chatType != null ? "/chats?chatType=" + chatType.name().toLowerCase() : "/chats";
        ResponseEntity<List<ChatDetail>> result = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(null, getHttpHeaders(senderId)),
                new ParameterizedTypeReference<>() {
                });

        var expectedChatsInResponse = switch (chatType) {
            case PRIVATE -> privateChats;
            case GROUP -> groupChats;
            case null -> ListUtils.union(privateChats, groupChats);
        };

        assert result.getBody() != null;
        var chatDetails = result.getBody();
        LOGGER.info("ChatDetails: {}", chatDetails);
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(chatDetails).hasSize(expectedChatsInResponse.size());

        var chatMap = expectedChatsInResponse.stream()
                .collect(Collectors.toMap(Chat::getId, Function.identity()));

        for (ChatDetail chatDetail : chatDetails) {
            assert chatDetail.getName() != null;
            if (chatType == ChatTypeDTO.PRIVATE) {
                assertThat(chatDetail.getChatType()).isEqualTo(ChatType.PRIVATE);
                assert chatDetail.getOtherUserId() != null;
                var otherUserId = chatMap.get(chatDetail.getChatId())
                        .getParticipants()
                        .stream()
                        .map(ChatParticipant::getUserId)
                        .filter(userId -> !userId.equals(senderId))
                        .findFirst()
                        .orElse(null);
                assertThat(chatDetail.getOtherUserId()).isEqualTo(otherUserId);
                assertThat(chatDetail.getName()).startsWith(TestUtils.USERNAME_PREFIX);
                assertThat(chatDetail.getImageUrl()).startsWith(TestUtils.URL_PREFIX);
            } else if (chatType == ChatTypeDTO.GROUP) {
                assertThat(chatDetail.getChatType()).isEqualTo(ChatType.GROUP);
                assert chatDetail.getOtherUserId() == null;
                var chat = chatMap.get(chatDetail.getChatId());
                assertThat(chatDetail.getName()).isEqualTo(chat.getName());
                assertThat(chatDetail.getImageUrl()).isEqualTo(chat.getImageUrl());
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

        ResponseEntity<Set<Long>> result = restTemplate.exchange(
                "/internal/chats/" + chat.getId() + "/participants/ids",
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
        ResponseEntity<Void> result = restTemplate.exchange(
                "/chats/" + chat.getId(), HttpMethod.PATCH, new HttpEntity<>(request, getHttpHeaders(senderId)), Void.class);

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
        mockGetUsers(request.userIdsToAdd());
        ResponseEntity<Void> result = restTemplate.exchange(
                "/chats/" + chat.getId() + "/participants",
                HttpMethod.PATCH, new HttpEntity<>(request, getHttpHeaders(senderId)), Void.class);

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

    @ParameterizedTest
    @EnumSource(ChatParticipantRoleDTO.class)
    void shouldChangeChatParticipantRoleWhenRequested(ChatParticipantRoleDTO chatParticipantRoleDTO) {
        Long senderId = RandomUtils.secure().randomLong();
        Long participantId = RandomUtils.secure().randomLong();
        Chat chat = createChat(false, List.of(participantId), senderId);
        chatRepository.save(chat);

        var request = new ModifyChatParticipantRole(participantId, chatParticipantRoleDTO);
        ResponseEntity<Void> result = restTemplate.exchange(
                "/chats/" + chat.getId() + "/participants/role",
                HttpMethod.PATCH,
                new HttpEntity<>(request, getHttpHeaders(senderId)),
                Void.class
        );

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        var savedChat = chatRepository.findWithParticipantsById(chat.getId()).orElseThrow();
        assertThat(savedChat.getParticipants())
                .filteredOn(participant -> participant.getUserId().equals(participantId))
                .singleElement()
                .extracting(ChatParticipant::getRole)
                .extracting(ChatParticipantRole::name)
                .isEqualTo(chatParticipantRoleDTO.name());
    }

    @Test
    void shouldDeleteChatParticipantWhenRequested() {
        int numberOfParticipants = 2;
        Long senderId = RandomUtils.secure().randomLong();
        var userIds = getRandomUserIds(numberOfParticipants);
        Chat chat = createChat(false, userIds, senderId);
        chatRepository.save(chat);

        ResponseEntity<Void> result = restTemplate.exchange(
                "/chats/" + chat.getId() + "/participants",
                HttpMethod.DELETE, new HttpEntity<>(null, getHttpHeaders(senderId)), Void.class);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        var savedChat = chatRepository.findWithParticipantsById(chat.getId()).orElse(null);
        assertThat(savedChat).isNotNull();
        var participants = savedChat.getParticipants().stream().map(ChatParticipant::getUserId).collect(Collectors.toSet());
        assertThat(participants).containsExactlyInAnyOrderElementsOf(userIds);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldGetChatWhenRequested(boolean isPrivate) {
        int numberOfParticipants = isPrivate ? 1 : 3;
        Long senderId = RandomUtils.secure().randomLong();
        var userIds = getRandomUserIds(numberOfParticipants);
        Chat chat = createChat(isPrivate, userIds, senderId);
        chatRepository.save(chat);

        mockGetUsers(userIds);
        ResponseEntity<ChatDetail> result = restTemplate.exchange(
                "/chats/" + chat.getId(),
                HttpMethod.GET, new HttpEntity<>(null, getHttpHeaders(senderId)),
                ChatDetail.class
        );

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody()).isNotNull();
        if (isPrivate) {
            assertThat(result.getBody().getName()).startsWith(TestUtils.USERNAME_PREFIX);
            assertThat(result.getBody().getImageUrl()).startsWith(TestUtils.URL_PREFIX);
        } else {
            assertThat(result.getBody().getName()).isEqualTo(chat.getName());
            assertThat(result.getBody().getImageUrl()).isEqualTo(chat.getImageUrl());
        }
        assertThat(result.getBody().getLastMessageAt()).isEqualTo(chat.getLastMessageAt());
        assertThat(result.getBody().getLastReadAt()).isNotNull();
        assertThat(result.getBody().getParticipants())
                .isNotNull()
                .hasSize(numberOfParticipants)
                .extracting(ParticipantDTO::userId)
                .containsExactlyInAnyOrderElementsOf(userIds);
    }

    @Test
    void shouldGetChatParticipantsWhenRequested() {
        int numberOfParticipants = 4;
        Long senderId = RandomUtils.secure().randomLong();
        var userIds = getRandomUserIds(numberOfParticipants);
        Chat chat = createChat(false, userIds, senderId);
        chatRepository.save(chat);

        mockGetUsers(userIds);
        ResponseEntity<List<ParticipantDTO>> result = restTemplate.exchange(
                "/chats/" + chat.getId() + "/participants",
                HttpMethod.GET, new HttpEntity<>(null, getHttpHeaders(senderId)),
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody()).isNotNull()
                .hasSize(numberOfParticipants)
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

        var request = new UpdateChatReadAtRequest(Instant.now().truncatedTo(ChronoUnit.MICROS));
        ResponseEntity<Void> result = restTemplate.exchange(
                "/chats/" + chat.getId() + "/participants/last_read_at", HttpMethod.PATCH,
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
        headers.set(USER_ID_HEADER, String.valueOf(senderId));
        return headers;
    }

}
