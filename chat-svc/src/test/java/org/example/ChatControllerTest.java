package org.example;

import org.example.application.chat.dto.ChatDTO;
import org.example.application.chat.dto.ChatParticipantsDTO;
import org.example.application.chat.dto.ChatRequest;
import org.example.application.chat.dto.ChatsResponse;
import org.example.domain.chat.entity.Chat;
import org.example.domain.chat.repository.ChatParticipantRepository;
import org.example.domain.chat.repository.ChatRepository;
import org.example.domain.chat.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import wiremock.org.apache.commons.lang3.RandomUtils;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.example.TestUtils.mockGetUsers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = IntegrationTestInitializer.class)
public class ChatControllerTest {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private ChatService chatService;
    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private ChatParticipantRepository chatParticipantRepository;

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
        mockGetUsers(userIds);

        HttpHeaders headers = new HttpHeaders();
        headers.set("userId", String.valueOf(senderId));
        var result = restTemplate.postForEntity("/chat",
                new HttpEntity<>(chatRequest, headers),
                Long.class
        );
        assert result.getBody() != null;
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(chatRepository.findById(result.getBody())).isNotNull();
        assertThat(chatParticipantRepository.findAllByChatId(result.getBody()))
                .hasSize(numberOfParticipants + 1);
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
        mockGetUsers(userIds);

        HttpHeaders headers = new HttpHeaders();
        headers.set("userId", String.valueOf(senderId));
        var result = restTemplate.postForEntity("/chat",
                new HttpEntity<>(chatRequest, headers),
                Long.class
        );
        assert result.getBody() != null;
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(chatRepository.findById(result.getBody())).isNotNull();
        assertThat(chatParticipantRepository.findAllByChatId(result.getBody()))
                .hasSize(2);
    }

    @Test
    public void shouldReturnUserChatsWhenRequested() {
        int numberOfGroupChats = 6;
        int numberOfPrivateChats = 10;
        Long senderId = RandomUtils.nextLong();
        IntStream.range(0, numberOfGroupChats)
                .forEach(i -> {
                    Chat chat = Chat.builder()
                            .name(randomAlphabetic(12))
                            .imageUrl(randomAlphabetic(12))
                            .isPrivate(false)
                            .build();
                    var userIds = IntStream.range(0, RandomUtils.nextInt(0, 10))
                            .mapToObj(j -> RandomUtils.nextLong())
                            .collect(Collectors.toSet());
                    chatService.createChat(senderId, chat, userIds);
                });
        IntStream.range(0, numberOfPrivateChats)
                .forEach(i -> {
                    Chat chat = Chat.builder()
                            .isPrivate(true)
                            .build();
                    chatService.createChat(senderId, chat, Set.of(RandomUtils.nextLong()));
                });

        HttpHeaders headers = new HttpHeaders();
        headers.set("userId", String.valueOf(senderId));
        var result = restTemplate.exchange("/chat",
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                ChatsResponse.class
        );
        assert result.getBody() != null;
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody().chats())
                .hasSize(numberOfGroupChats + numberOfPrivateChats);
        assertThat(result.getBody().chats().stream().filter(ChatDTO::isPrivate).count())
                .isEqualTo(numberOfPrivateChats);
        assertThat(result.getBody().chats().stream().filter(c -> !c.isPrivate()).count())
                .isEqualTo(numberOfGroupChats);
    }

    @Test
    public void shouldReturnChatParticipantsWhenRequested() {
        int numberOfParticipants = 10;
        Long senderId = RandomUtils.nextLong();
        Chat chat = Chat.builder()
                .name(randomAlphabetic(12))
                .imageUrl(randomAlphabetic(12))
                .isPrivate(false)
                .build();
        var userIds = IntStream.range(0, RandomUtils.nextInt(0, numberOfParticipants))
                .mapToObj(j -> RandomUtils.nextLong())
                .collect(Collectors.toSet());
        chatService.createChat(senderId, chat, userIds);

        var result = restTemplate.getForEntity("/internal/chat?chatId=" + chat.getId(),
                ChatParticipantsDTO.class
        );
        assert result.getBody() != null;

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();

        userIds.add(senderId);
        assertThat(result.getBody().userIds()).hasSize(userIds.size());
        assertThat(result.getBody().userIds())
                .containsExactlyInAnyOrderElementsOf(userIds);
    }

}
