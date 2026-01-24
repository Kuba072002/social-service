package org.example;

import org.apache.commons.text.RandomStringGenerator;
import org.example.application.dto.SignInRequest;
import org.example.application.dto.SignInResponse;
import org.example.application.dto.SignUpRequest;
import org.example.application.dto.UserDTO;
import org.example.application.service.UserMapper;
import org.example.domain.entity.User;
import org.example.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;
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
    private PasswordEncoder passwordEncoder;
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
        var user = createUser(passwordEncoder.encode(password));
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
    void shouldGetUser() {
        var user = createUser();
        userRepository.save(user);

        var response = restTestClient.get()
                .uri("/users?userName=" + user.getUserName())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserDTO.class)
                .returnResult()
                .getResponseBody();

        assertThat(response)
                .isNotNull()
                .isEqualTo(userMapper.toUserDTO(user));
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
        return createUser(randomAlphabetic(12));
    }

    private static User createUser(String password) {
        var userName = randomAlphabetic(12);
        return User.builder().userName(userName).email(userName + "@mail.com").password(password).build();
    }

    public static String randomAlphabetic(int length) {
        return STRING_GENERATOR.generate(length);
    }
}
