package org.example;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.example.common.JsonUtils;
import org.example.domain.chat.entity.Chat;
import org.example.domain.chat.entity.ChatParticipant;
import org.example.domain.chat.entity.ChatParticipantRole;
import org.example.domain.chat.entity.ChatType;
import org.example.domain.user.UserDTO;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TestUtils {
    public static final String USERNAME_PREFIX = "username:";
    public static final String URL_PREFIX = "url:";

    private static final RandomStringGenerator STRING_GENERATOR = new RandomStringGenerator.Builder()
            .withinRange('A', 'Z')
            .withinRange('a', 'z')
            .filteredBy(Character::isLetter)
            .get();

    public static void mockGetUsers(Set<Long> userIds) {
        IntegrationTestInitializer.WIREMOCK.stubFor(
                WireMock.post(WireMock.urlPathEqualTo("/internal/users"))
                        .withRequestBody(WireMock.equalToJson(JsonUtils.writeToJson(userIds), true, false)) // Ensures proper JSON comparison
                        .willReturn(WireMock.aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(JsonUtils.writeToJson(createUsers(userIds))))
        );
    }

    private static List<UserDTO> createUsers(Set<Long> userIds) {
        return userIds.stream()
                .map(userId -> new UserDTO(
                                userId,
                                USERNAME_PREFIX + randomAlphabetic(12),
                                randomAlphabetic(12),
                                URL_PREFIX + randomAlphabetic(12),
                                null
                        )
                ).toList();
    }

    public static String randomAlphabetic(int length) {
        return STRING_GENERATOR.generate(length);
    }

    private static Chat createChat(boolean isPrivate) {
        var builder = Chat.builder()
                .chatType(isPrivate ? ChatType.PRIVATE : ChatType.GROUP)
                .lastMessageAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        if (!isPrivate) {
            builder.name(randomAlphabetic(12)).imageUrl(randomAlphabetic(12));
        }
        return builder.build();
    }

    public static Chat createChat(boolean isPrivate, Collection<Long> userIds) {
        return createChat(isPrivate, userIds, null);
    }

    public static Chat createChat(boolean isPrivate, Collection<Long> userIds, Long userId) {
        Chat chat = createChat(isPrivate);
        if (isPrivate) {
            chat.setPrivatePairKey(createPrivatePairKey(userIds, userId));
        }
        var participants = userIds.stream()
                .map(id -> new ChatParticipant(chat, id))
                .peek(participant -> participant.setRole(ChatParticipantRole.MEMBER))
                .peek(cp -> cp.setLastReadAt(
                        Instant.now().minusSeconds(cp.getUserId() % 100).truncatedTo(ChronoUnit.MICROS)))
                .collect(Collectors.toCollection(ArrayList::new));
        if (userId != null) participants.add(new ChatParticipant(chat, userId, ChatParticipantRole.ADMIN));
        chat.setParticipants(participants);
        return chat;
    }

    private static String createPrivatePairKey(Collection<Long> userIds, Long userId) {
        Stream<Long> userIdsStream;
        if (userIds.size() == 1 && userId != null) {
            userIdsStream = Stream.of(userId, userIds.iterator().next());
        } else if (userIds.size() == 2) {
            userIdsStream = userIds.stream();
        } else {
            throw new IllegalArgumentException("Private chat must have exactly 2 participants");
        }
        return userIdsStream
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(":"));
    }

    public static Set<Long> getRandomUserIds(int numberOfParticipants) {
        return IntStream.range(0, numberOfParticipants)
                .mapToObj(i -> RandomUtils.secure().randomLong())
                .collect(Collectors.toSet());
    }
}
