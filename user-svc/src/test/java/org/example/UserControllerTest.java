package org.example;

import org.apache.commons.text.RandomStringGenerator;
import org.example.application.dto.RefreshTokenRequest;
import org.example.application.dto.SignInRequest;
import org.example.application.dto.SignInResponse;
import org.example.application.dto.SignUpRequest;
import org.example.application.dto.UserDTO;
import org.example.application.service.TokenService;
import org.example.application.service.UserMapper;
import org.example.domain.entity.RefreshToken;
import org.example.domain.entity.User;
import org.example.domain.repository.RefreshTokenRepository;
import org.example.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = IntegrationTestInitializer.class)
@AutoConfigureRestTestClient
class UserControllerTest {

    @Autowired
    private RestTestClient restTestClient;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private UserMapper userMapper;

    private static final RandomStringGenerator STRING_GENERATOR = new RandomStringGenerator.Builder()
            .withinRange('A', 'Z')
            .withinRange('a', 'z')
            .filteredBy(Character::isLetter)
            .get();


    @Test
    void shouldCreateUser() {
        var password = randomAlphabetic(12);
        var userName = randomAlphabetic(12);
        var request = new SignUpRequest(userName, userName + "@mail.com", password, password);

        restTestClient.post()
                .uri("/register")
                .body(request)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Void.class);

        var user = userRepository.findByUserName(request.userName());
        assertThat(user).isNotNull();
    }

    @Test
    void shouldLoginUser() {
        var password = randomAlphabetic(12);
        var user = createUserWithPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        var request = new SignInRequest(user.getEmail(), password);
        var response = restTestClient.post()
                .uri("/login")
                .body(request)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(SignInResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.user()).isEqualTo(userMapper.toUserDTO(user));
    }

    @Test
    void shouldRefreshToken() throws InterruptedException {
        var password = randomAlphabetic(12);
        var user = createUserWithPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        var request = new SignInRequest(user.getEmail(), password);
        var loginResponse = restTestClient.post()
                .uri("/login")
                .body(request)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(SignInResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(loginResponse).isNotNull();
        assertThat(loginResponse.user()).isEqualTo(userMapper.toUserDTO(user));
        var refreshHashTokens = refreshTokenRepository.findByUserId(user.getId())
                .stream()
                .map(RefreshToken::getHashToken)
                .toList();
        assertThat(refreshHashTokens)
                .contains(tokenService.hash(loginResponse.refreshToken()));
        var loginTokenExpiration = tokenService.validateAndGetClaims(loginResponse.refreshToken(), TokenService.REFRESH_TYPE).getExpiration();

        Thread.sleep(500);

        var response = restTestClient.post()
                .uri("/refresh")
                .body(new RefreshTokenRequest(loginResponse.refreshToken()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(SignInResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.user()).isEqualTo(userMapper.toUserDTO(user));
        refreshHashTokens = refreshTokenRepository.findByUserId(user.getId())
                .stream()
                .map(RefreshToken::getHashToken)
                .toList();

        assertThat(refreshHashTokens)
                .contains(tokenService.hash(response.refreshToken()))
                .doesNotContain(tokenService.hash(loginResponse.refreshToken()));
        var refreshTokenExpiration = tokenService.validateAndGetClaims(response.refreshToken(), TokenService.REFRESH_TYPE).getExpiration();
        assertThat(refreshTokenExpiration).isAfter(loginTokenExpiration);
    }

    @Test
    void shouldGetUsersByUsername() {
        List<Integer> expected = List.of(1, 2, 4, 5);
        String prefix = randomAlphabetic(5);
        Map<Integer, User> map = Map.of(
                1, createUserWithUserName(prefix + randomAlphabetic(8)),
                2, createUserWithUserName(prefix + randomAlphabetic(8)),
                3, createUserWithUserName(randomAlphabetic(10)),
                4, createUserWithUserName(randomAlphabetic(7) + prefix),
                5, createUserWithUserName(randomAlphabetic(3) + prefix + randomAlphabetic(7))
        );
        userRepository.saveAll(map.values());

        var response = restTestClient.get()
                .uri("/users?userName=" + prefix)
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(new ParameterizedTypeReference<List<UserDTO>>() {
                })
                .getResponseBody();

        assert response != null;
        assertThat(response).hasSize(expected.size());
        var expectedDTOs = map.entrySet().stream()
                .filter(entry -> expected.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .map(userMapper::toUserDTO)
                .toList();
        assertThat(response).containsAll(expectedDTOs);
    }

    @Test
    void shouldGetUsers() {
        var user1 = createUser();
        var user2 = createUser();
        userRepository.saveAll(List.of(user1, user2));

        var response = restTestClient.post()
                .uri("/internal/users")
                .body(Set.of(user1.getId(), user2.getId()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<Set<UserDTO>>() {
                })
                .returnResult()
                .getResponseBody();

        assertThat(response)
                .isNotNull()
                .hasSize(2)
                .containsAll(List.of(userMapper.toUserDTO(user1), userMapper.toUserDTO(user2)));
    }

    private static User createUser() {
        return createUser(randomAlphabetic(12), randomAlphabetic(12));
    }

    private static User createUser(String userName, String password) {
        return User.builder()
                .userName(userName)
                .email(userName + "@mail.com")
                .password(password)
                .lastSeenAt(Instant.now().truncatedTo(ChronoUnit.MICROS))
                .build();
    }

    private static User createUserWithUserName(String userName) {
        return createUser(userName, randomAlphabetic(12));
    }

    private static User createUserWithPassword(String password) {
        return createUser(randomAlphabetic(12), password);
    }

    public static String randomAlphabetic(int length) {
        return STRING_GENERATOR.generate(length);
    }
}
