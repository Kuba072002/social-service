package org.example;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class WebclientConfiguration {
    @Value("${user.service.url}")
    private String userServiceUrl;
    @Value("${chat.service.url}")
    private String chatServiceUrl;
    @Value("${message.service.url}")
    private String messageServiceUrl;

    private final AuthenticationService authenticationFilter;

    @Bean
    public WebClient userSvcClient() {
        return WebClient.create(userServiceUrl);
    }

    @Bean
    public WebClient chatSvcClient() {
        return WebClient.create(chatServiceUrl);
    }

    @Bean
    public WebClient messageSvcClient() {
        return WebClient.create(messageServiceUrl);
    }

    @Bean
    public HttpGraphQlClient userSvcGraphQlClient(WebClient userSvcClient) {
        return HttpGraphQlClient.create(userSvcClient).mutate().build();
    }

    @Bean
    public HttpGraphQlClient chatSvcGraphQlClient(WebClient chatSvcClient) {
        return HttpGraphQlClient.create(chatSvcClient).mutate().build();
    }

    @Bean
    public HttpGraphQlClient messageSvcGraphQlClient(WebClient messageSvcClient) {
        return HttpGraphQlClient.create(messageSvcClient).mutate().build();
    }

}
