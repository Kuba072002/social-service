package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.example.domain.user.UserDTO;

import java.util.List;
import java.util.Set;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

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
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot convert to json");
        }
    }
}
