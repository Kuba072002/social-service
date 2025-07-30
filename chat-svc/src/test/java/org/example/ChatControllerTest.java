package org.example;

import org.example.domain.chat.entity.Chat;
import org.example.domain.chat.entity.ChatParticipant;
import org.example.domain.chat.repository.ChatParticipantRepository;
import org.example.domain.chat.repository.ChatRepository;
import org.example.domain.event.ChatEvent;
import org.example.domain.event.ChatEventPublisher;
import org.example.domain.event.ChatEventType;
import org.example.dto.chat.*;
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

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.example.TestUtils.mockGetUser;
import static org.example.TestUtils.mockGetUsers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = IntegrationTestInitializer.class)
public class ChatControllerTest {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private ChatParticipantRepository chatParticipantRepository;
    @MockitoBean
    private ChatEventPublisher chatEventPublisher;
    ArgumentCaptor<ChatEvent> eventCaptor = ArgumentCaptor.forClass(ChatEvent.class);

    @Test
    public void shouldCreateChatWhenRequested() {
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
        assertThat(chatRepository.findById(result.getBody())).isNotNull();
        assertThat(chatParticipantRepository.findByChatId(result.getBody()))
                .hasSize(numberOfParticipants + 1);
        Mockito.verify(chatEventPublisher, Mockito.times(1)).sendEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().chatId()).isEqualTo(result.getBody());
        assertThat(eventCaptor.getValue().type()).isEqualTo(ChatEventType.CREATE);
    }

    @Test
    public void shouldCreatePrivateChatWhenRequested() {
        var userIds = Set.of(RandomUtils.nextLong());
        var senderId = RandomUtils.nextLong();
        ChatRequest chatRequest = new ChatRequest(
                null,
                null,
                true,
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
        assertThat(chatRepository.findById(result.getBody())).isNotNull();
        assertThat(chatParticipantRepository.findByChatId(result.getBody()))
                .hasSize(2);
        Mockito.verify(chatEventPublisher, Mockito.times(1)).sendEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().chatId()).isEqualTo(result.getBody());
        assertThat(eventCaptor.getValue().type()).isEqualTo(ChatEventType.CREATE);
    }

    @Test
    public void shouldReturnUserChatsWhenRequested() {
        int numberOfGroupChats = 6;
        int numberOfPrivateChats = 3;
        Long senderId = RandomUtils.nextLong();
        var groupChats = IntStream.range(0, numberOfGroupChats)
                .mapToObj(i -> TestUtils.createGroupChat(senderId, RandomUtils.nextInt(0, 10)))
                .toList();
        chatRepository.saveAll(groupChats);
        var privateChats = IntStream.range(0, numberOfPrivateChats)
                .mapToObj(i -> TestUtils.createPrivateChat(senderId))
                .toList();
        chatRepository.saveAll(privateChats);
        mockGetUser(senderId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("userId", String.valueOf(senderId));
        ResponseEntity<List<ChatDetail>> result = restTemplate.exchange("/chats?isPrivate=false",
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                new ParameterizedTypeReference<>() {
                });

        assert result.getBody() != null;
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody())
                .hasSize(numberOfGroupChats);
        assertThat(result.getBody().stream().filter(c -> !c.getPrivate()).count())
                .isEqualTo(numberOfGroupChats);
    }

    @Test
    public void shouldReturnUserPrivateChatsWhenRequested() {
        int numberOfGroupChats = 2;
        int numberOfPrivateChats = 3;
        Long senderId = RandomUtils.nextLong();
        var groupChats = IntStream.range(0, numberOfGroupChats)
                .mapToObj(i -> TestUtils.createGroupChat(senderId, RandomUtils.nextInt(0, 5)))
                .toList();
        chatRepository.saveAll(groupChats);
        var privateChats = IntStream.range(0, numberOfPrivateChats)
                .mapToObj(i -> TestUtils.createPrivateChat(senderId))
                .toList();
        chatRepository.saveAll(privateChats);
        var otherUserIds = privateChats.stream()
                .flatMap(c -> c.getParticipants().stream())
                .map(ChatParticipant::getUserId)
                .filter(id -> !id.equals(senderId))
                .collect(Collectors.toSet());

        mockGetUser(senderId);
        mockGetUsers(otherUserIds);

        HttpHeaders headers = new HttpHeaders();
        headers.set("userId", String.valueOf(senderId));
        ResponseEntity<List<ChatDetail>> result = restTemplate.exchange("/chats?isPrivate=true",
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                new ParameterizedTypeReference<>() {
                });

        assert result.getBody() != null;
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody())
                .hasSize(numberOfPrivateChats);
        assertThat(result.getBody().stream().filter(ChatDetail::getPrivate).count())
                .isEqualTo(numberOfPrivateChats);
        var mapResponseChatIdOtherUserId = result.getBody()
                .stream()
                .collect(Collectors.toMap(
                        ChatDetail::getChatId,
                        ChatDetail::getOtherUser
                ));
        privateChats.forEach(chat -> {
            var otherUserId = chat.getParticipants().stream()
                    .map(ChatParticipant::getUserId)
                    .filter(userId -> !userId.equals(senderId))
                    .findFirst();
            assert otherUserId.isPresent();
            assertThat(otherUserId.get())
                    .isEqualTo(mapResponseChatIdOtherUserId.get(chat.getId()));
        });
    }

    @Test
    public void shouldModifyChatWhenRequested() {
        int numberOfParticipants = 10;
        Long senderId = RandomUtils.nextLong();
        Chat chat = TestUtils.createGroupChat(senderId, numberOfParticipants);
        chatRepository.save(chat);

        mockGetUser(senderId);
        HttpHeaders headers = new HttpHeaders();
        headers.set("userId", String.valueOf(senderId));
        var request = new ModifyChatRequest(
                randomAlphabetic(22), randomAlphabetic(25)
        );
        ResponseEntity<Void> result = restTemplate.exchange(
                "/chats/" + chat.getId(),
                HttpMethod.PUT,
                new HttpEntity<>(request, headers),
                Void.class);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();

        var modifiedChat = chatRepository.findById(chat.getId());
        assertThat(modifiedChat.isPresent()).isTrue();
        assertThat(modifiedChat.get().getName()).isEqualTo(request.name());
        assertThat(modifiedChat.get().getImageUrl()).isEqualTo(request.imageUrl());
    }

    @Test
    public void shouldModifyChatParticipantsWhenRequested() {
        int numberOfParticipants = 4;
        int numberOfParticipantsToDelete = 2;
        int numberOfParticipantsToAdd = 3;
        Long senderId = RandomUtils.nextLong();
        Chat chat = TestUtils.createGroupChat(senderId, numberOfParticipants);
        chatRepository.save(chat);

        var userIds = chat.getParticipants().stream()
                .map(ChatParticipant::getUserId)
                .collect(Collectors.toSet());
        var userIdsToDelete = userIds.stream()
                .filter(id -> !id.equals(senderId))
                .limit(numberOfParticipantsToDelete)
                .collect(Collectors.toSet());
        userIds.removeAll(userIdsToDelete);
        var userIdsToAdd = IntStream.range(0, numberOfParticipantsToAdd)
                .mapToObj(i -> RandomUtils.nextLong())
                .collect(Collectors.toSet());

        mockGetUser(senderId);
        mockGetUsers(userIdsToAdd);
        HttpHeaders headers = new HttpHeaders();
        headers.set("userId", String.valueOf(senderId));
        var request = new ModifyChatParticipantsRequest(
                userIdsToAdd, userIdsToDelete
        );
        ResponseEntity<Void> result = restTemplate.exchange(
                "/chats/" + chat.getId() + "/participants",
                HttpMethod.PUT,
                new HttpEntity<>(request, headers),
                Void.class);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();

        var modifiedParticipantIds = chatParticipantRepository.findByChatId(chat.getId())
                .stream()
                .map(ChatParticipant::getUserId)
                .collect(Collectors.toSet());
        assertThat(modifiedParticipantIds.isEmpty()).isFalse();
        assertThat(modifiedParticipantIds)
                .hasSize(numberOfParticipants - numberOfParticipantsToDelete + numberOfParticipantsToAdd);
        assertThat(modifiedParticipantIds).containsAll(userIds);
        assertThat(modifiedParticipantIds).containsAll(userIdsToAdd);
        Mockito.verify(chatEventPublisher, Mockito.times(1)).sendEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().chatId()).isEqualTo(chat.getId());
        assertThat(eventCaptor.getValue().type()).isEqualTo(ChatEventType.MODIFY);
    }

    @Test
    public void shouldDeleteChatParticipantWhenRequested() {
        int numberOfParticipants = 4;
        Long senderId = RandomUtils.nextLong();
        Chat chat = TestUtils.createGroupChat(senderId, numberOfParticipants);
        chatRepository.save(chat);

        var userIds = chat.getParticipants().stream()
                .map(ChatParticipant::getUserId)
                .collect(Collectors.toSet());

        mockGetUser(senderId);
        HttpHeaders headers = new HttpHeaders();
        headers.set("userId", String.valueOf(senderId));
        ResponseEntity<Void> result = restTemplate.exchange(
                "/chats/" + chat.getId() + "/participants",
                HttpMethod.DELETE,
                new HttpEntity<>(null, headers),
                Void.class);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();

        var modifiedParticipantIds = chatParticipantRepository.findByChatId(chat.getId())
                .stream()
                .map(ChatParticipant::getUserId)
                .collect(Collectors.toSet());

        userIds.remove(senderId);
        assertThat(modifiedParticipantIds.isEmpty()).isFalse();
        assertThat(modifiedParticipantIds)
                .hasSize(numberOfParticipants - 1);
        assertThat(modifiedParticipantIds).containsAll(userIds);
        assertThat(modifiedParticipantIds).doesNotContain(senderId);
        Mockito.verify(chatEventPublisher, Mockito.times(1)).sendEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().chatId()).isEqualTo(chat.getId());
        assertThat(eventCaptor.getValue().type()).isEqualTo(ChatEventType.MODIFY);
    }

    @Test
    public void shouldUpdateLastReadAtWhenRequested() {
        int numberOfParticipants = 6;
        Long senderId = RandomUtils.nextLong();
        Chat chat = TestUtils.createGroupChat(senderId, numberOfParticipants);
        chatRepository.save(chat);

        mockGetUser(senderId);
        Instant lastReadAt = Instant.now().plusSeconds(90)
                .truncatedTo(ChronoUnit.MICROS);
        HttpHeaders headers = new HttpHeaders();
        headers.set("userId", String.valueOf(senderId));
        ResponseEntity<Void> result = restTemplate.exchange(
                "/chats/" + chat.getId() + "/participants/last_read_at",
                HttpMethod.PUT,
                new HttpEntity<>(new UpdateChatReadAtRequest(lastReadAt.atOffset(ZoneOffset.UTC)), headers),
                Void.class);
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();

        var chatParticipant = chatParticipantRepository.findByChatIdAndUserId(chat.getId(), senderId);
        assert chatParticipant.isPresent();
        assertThat(chatParticipant.get().getLastReadAt()).isEqualTo(lastReadAt);
    }

    @Test
    public void shouldDeleteChatWhenRequested() {
        int numberOfParticipants = 10;
        Long senderId = RandomUtils.nextLong();
        Chat chat = TestUtils.createGroupChat(senderId, numberOfParticipants);
        chatRepository.save(chat);

        mockGetUser(senderId);
        HttpHeaders headers = new HttpHeaders();
        headers.set("userId", String.valueOf(senderId));
        ResponseEntity<Void> result = restTemplate.exchange(
                "/chats/" + chat.getId(),
                HttpMethod.DELETE,
                new HttpEntity<>(null, headers),
                Void.class);
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();

        var fetchedChat = chatRepository.findById(chat.getId());
        assertThat(fetchedChat.isPresent()).isFalse();
        var chatParticipants = chatParticipantRepository.findByChatId(chat.getId());
        assertThat(chatParticipants.size()).isZero();
    }

    @Test
    public void shouldReturnChatParticipantsWhenRequested() {
        int numberOfParticipants = 7;
        Long senderId = RandomUtils.nextLong();
        Chat chat = TestUtils.createGroupChat(senderId, numberOfParticipants);
        chatRepository.save(chat);
        var userIds = chat.getParticipants().stream()
                .map(ChatParticipant::getUserId)
                .filter(id -> !id.equals(senderId))
                .collect(Collectors.toSet());

        mockGetUser(senderId);
        mockGetUsers(userIds);
        HttpHeaders headers = new HttpHeaders();
        headers.set("userId", String.valueOf(senderId));
        ResponseEntity<List<ParticipantDTO>> result = restTemplate.exchange(
                "/chats/" + chat.getId() + "/participants",
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                new ParameterizedTypeReference<>() {
                });
        assert result.getBody() != null;

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();

        assertThat(result.getBody()).hasSize(userIds.size());
        assertThat(result.getBody().stream()
                .map(ParticipantDTO::userId)
                .collect(Collectors.toSet()))
                .containsExactlyInAnyOrderElementsOf(userIds);
    }

    @Test
    public void shouldReturnChatParticipantIdsWhenRequested() {
        int numberOfParticipants = 10;
        Long senderId = RandomUtils.nextLong();
        Chat chat = TestUtils.createGroupChat(senderId, numberOfParticipants);
        chatRepository.save(chat);
        var userIds = chat.getParticipants().stream()
                .map(ChatParticipant::getUserId)
                .collect(Collectors.toSet());

        mockGetUser(senderId);
        HttpHeaders headers = new HttpHeaders();
        headers.set("userId", String.valueOf(senderId));
        ResponseEntity<Set<Long>> result = restTemplate.exchange(
                "/internal/chats/participants/ids?chatId=" + chat.getId(),
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                new ParameterizedTypeReference<>() {
                });
        assert result.getBody() != null;

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();

        assertThat(result.getBody()).hasSize(userIds.size());
        assertThat(result.getBody())
                .containsExactlyInAnyOrderElementsOf(userIds);
    }

}
