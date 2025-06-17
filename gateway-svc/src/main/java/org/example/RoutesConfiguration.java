package org.example;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RoutesConfiguration {
    @Value("${user.service.url}")
    private String userServiceUrl;
    @Value("${chat.service.url}")
    private String chatServiceUrl;
    @Value("${message.service.url}")
    private String messageServiceUrl;

    private final AuthenticationFilter authenticationFilter;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-svc-route", r -> r
                        .path("/user-svc/**")
                        .and().not(rp -> rp.path("/user-svc/internal/**"))
                        .uri(userServiceUrl)
                )
                .route("chat-svc-route", r -> r
                        .path("/chat-svc/**")
                        .and().not(rp -> rp.path("/chat-svc/internal/**"))
                        .filters(f -> f.filter(authenticationFilter))
                        .uri(chatServiceUrl)
                )
                .route("message-svc-route", r -> r
                        .path("/message-svc/**")
                        .and().not(rp -> rp.path("/message-svc/internal/**"))
                        .filters(f -> f.filter(authenticationFilter))
                        .uri(messageServiceUrl)
                )
                .build();
    }
}
