package org.example;

import org.example.application.dto.SignInRequest;
import org.example.application.dto.SignInResponse;
import org.example.application.dto.SignUpRequest;
import org.example.application.dto.UserDTO;
import org.example.application.service.UserMapper;
import org.example.domain.entity.User;
import org.example.domain.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Set;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = IntegrationTestInitializer.class)
public class UserControllerTest {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserMapper userMapper;


    @Test
    void shouldCreateUser() {
        var password = randomAlphabetic(12);
        var userName = randomAlphabetic(12);
        var request = new SignUpRequest(userName, userName + "@mail.com", password, password);

        var response = restTemplate.postForEntity("/signUp", request, Void.class);

        Assertions.assertEquals(201, response.getStatusCode().value());
        var user = userRepository.findByUserName(request.userName());
        Assertions.assertNotNull(user);
    }

    @Test
    void shouldLoginUser() {
        var password = randomAlphabetic(12);
        var user = createUser(passwordEncoder.encode(password));
        userRepository.save(user);

        var request = new SignInRequest(user.getEmail(), password);
        var response = restTemplate.postForEntity("/signIn", request, SignInResponse.class);

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(userMapper.toUserDTO(user), response.getBody().user());
    }

    @Test
    void shouldGetUser() {
        var user = createUser();
        userRepository.save(user);

        var response = restTemplate.getForEntity("/users?userName=" + user.getUserName(), UserDTO.class);

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertNotNull(response.getBody());
        var expected = new UserDTO(user.getId(), user.getUserName(), user.getEmail(), user.getImageUrl());
        Assertions.assertEquals(expected, response.getBody());
    }

    @Test
    void shouldGetUsers() {
        var user1 = createUser();
        var user2 = createUser();
        userRepository.saveAll(List.of(user1, user2));

        HttpEntity<Set<Long>> request = new HttpEntity<>(Set.of(user1.getId(), user2.getId()));
        ResponseEntity<Set<UserDTO>> response = restTemplate.exchange(
                "/internal/users",
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<>() {
                });

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertNotNull(response.getBody());
        var expected1 = new UserDTO(user1.getId(), user1.getUserName(), user1.getEmail(), user1.getImageUrl());
        var expected2 = new UserDTO(user2.getId(), user2.getUserName(), user2.getEmail(), user2.getImageUrl());
        Assertions.assertEquals(2, response.getBody().size());
        assertThat(response.getBody()).containsAll(List.of(expected1, expected2));
    }

    private User createUser() {
        return createUser(randomAlphabetic(12));
    }

    private User createUser(String password) {
        var userName = randomAlphabetic(12);
        return User.builder().userName(userName).email(userName + "@mail.com").password(password).build();
    }
}
