package org.example;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.nio.charset.StandardCharsets;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.example.AuthenticationFilter.USER_ID_HEADER;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = IntegrationTestInitializer.class)
@AutoConfigureWebTestClient
class GatewayServiceApplicationTests {
    @Autowired
    private WebTestClient webTestClient;

    @Test
    void contextLoads() {
    }

    @Test
    void user_service_should_not_require_auth() {
        IntegrationTestInitializer.WIREMOCK.stubFor(
                get("/user-svc/profile")
                        .willReturn(ok("user-ok"))
        );

        webTestClient.get()
                .uri("/user-svc/profile")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("user-ok");
    }

    @Test
    void chat_service_should_reject_request_without_jwt() {
        webTestClient.get()
                .uri("/chat-svc/chats")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void chat_service_should_forward_request_with_valid_jwt() {
        IntegrationTestInitializer.WIREMOCK.stubFor(
                get("/chat-svc/chats")
                        .withHeader(USER_ID_HEADER, equalTo("42"))
                        .willReturn(ok("chat-ok"))
        );

        webTestClient.get()
                .uri("/chat-svc/chats")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validJwt("42"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("chat-ok");
    }

    @Test
    void message_service_should_reject_without_jwt() {
        webTestClient.get()
                .uri("/message-svc/messages")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void invalid_jwt_should_return_unauthorized() {
        webTestClient.get()
                .uri("/chat-svc/chats")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.token.value")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void chat_internal_path_should_not_be_routed_through_gateway() {
        // Even if backend has a stub, gateway must not forward to it
        IntegrationTestInitializer.WIREMOCK.stubFor(
                get("/chat-svc/internal/health")
                        .willReturn(ok("internal-ok"))
        );

        webTestClient.get()
                .uri("/chat-svc/internal/health")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void message_service_should_pass_ws_without_jwt() {
        IntegrationTestInitializer.WIREMOCK.stubFor(
                get("/message-svc/ws/info")
                        .willReturn(ok("ok"))
        );

        webTestClient.get()
                .uri("/message-svc/ws/info")
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    private String validJwt(String userId) {
        return Jwts.builder()
                .subject(userId)
                .signWith(Keys.hmacShaKeyFor(
                        "long_and_secure_jwt_secret_for_development"
                                .getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
