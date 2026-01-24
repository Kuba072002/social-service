package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.example.domain.chat.entity.Chat;
import org.example.domain.chat.entity.ChatParticipant;
import org.example.domain.user.UserDTO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.example.common.Constants.ADMIN_ROLE;

public class TestUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final RandomStringGenerator STRING_GENERATOR = new RandomStringGenerator.Builder()
            .withinRange('A', 'Z')
            .withinRange('a', 'z')
            .filteredBy(Character::isLetter)
            .get();

    public static void mockGetUser(Long userId) {
        IntegrationTestInitializer.WIREMOCK.stubFor(
                WireMock.get(WireMock.urlPathEqualTo("/internal/users/" + userId))
                        .willReturn(WireMock.aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(convertToJson(createUsers(Set.of(userId)).getFirst()))
                        )
        );
    }

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

    private static List<UserDTO> createUsers(Set<Long> userIds) {
        return userIds.stream()
                .map(userId -> new UserDTO(
                                userId,
                                randomAlphabetic(12),
                                randomAlphabetic(12),
                                randomAlphabetic(12)
                        )
                ).toList();
    }

    public static String convertToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot convert to json");
        }
    }

    public static String randomAlphabetic(int length) {
        return STRING_GENERATOR.generate(length);
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

    public static Chat createChat(boolean isPrivate, Collection<Long> userIds) {
        return createChat(isPrivate, userIds, null);
    }

    public static Chat createChat(boolean isPrivate, Collection<Long> userIds, Long userId) {
        Chat chat = createChat(isPrivate);
        var userMap = userIds.stream()
                .map(id -> new ChatParticipant(chat, id))
                .collect(Collectors.toMap(ChatParticipant::getUserId, Function.identity()));
        if (userId != null) userMap.put(userId, new ChatParticipant(chat, userId, ADMIN_ROLE));
        chat.setParticipants(new ArrayList<>(userMap.values()));
        return chat;
    }

    public static Set<Long> getRandomUserIds(int numberOfParticipants) {
        return IntStream.range(0, numberOfParticipants)
                .mapToObj(i -> RandomUtils.secure().randomLong())
                .collect(Collectors.toSet());
    }
}
