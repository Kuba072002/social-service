package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.example.dto.user.UserDTO;

import java.util.List;
import java.util.Set;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

public class TestUtils {

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

}
