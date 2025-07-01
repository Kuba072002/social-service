package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.example.domain.chat.entity.Chat;
import org.example.domain.chat.entity.ChatParticipant;
import org.example.domain.user.UserDTO;
import wiremock.org.apache.commons.lang3.RandomUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.example.common.Constants.ADMIN_ROLE;

public class TestUtils {

    public static void mockGetUsers(Set<Long> userIds) {
        IntegrationTestInitializer.WIREMOCK.stubFor(
                WireMock.post(WireMock.urlPathEqualTo("/internal/users"))
                        .withRequestBody(WireMock.equalToJson(convertToJson(userIds), true, false)) // Ensures proper JSON comparison
                        .willReturn(WireMock.aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(convertToJson(createUsers(userIds))))
        );
    }

    public static void mockGetUser(Long userId) {
        IntegrationTestInitializer.WIREMOCK.stubFor(
                WireMock.get(WireMock.urlPathEqualTo("/internal/users/" + userId))
                        .willReturn(WireMock.aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(convertToJson(createUser(userId))))
        );
    }

    private static List<UserDTO> createUsers(Set<Long> userIds) {
        return userIds.stream()
                .map(TestUtils::createUser)
                .toList();
    }

    private static UserDTO createUser(Long userId) {
        return new UserDTO(
                userId,
                randomAlphabetic(12),
                randomAlphabetic(12),
                randomAlphabetic(12)
        );
    }

    public static String convertToJson(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot convert to json");
        }
    }

    public static Chat createGroupChat(Long userId, int numberOfParticipants) {
        Chat chat = Chat.builder()
                .isPrivate(false)
                .name(randomAlphabetic(12))
                .imageUrl(randomAlphabetic(12))
                .build();
        chat.setParticipants(IntStream.range(0, numberOfParticipants - 1)
                .mapToObj(j -> new ChatParticipant(chat, RandomUtils.nextLong()))
                .collect(Collectors.toList()));
        chat.getParticipants().add(new ChatParticipant(chat, userId, ADMIN_ROLE));
        return chat;
    }

    public static Chat createPrivateChat(Long userId) {
        Chat chat = Chat.builder()
                .isPrivate(true)
                .build();
        chat.setParticipants(List.of(
                new ChatParticipant(chat, RandomUtils.nextLong(), ADMIN_ROLE),
                new ChatParticipant(chat, userId, ADMIN_ROLE)
        ));
        return chat;
    }
}
