package org.example.application.config;

import lombok.Getter;
import lombok.Setter;
import org.example.domain.chat.ChatService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.zalando.logbook.spring.LogbookClientHttpRequestInterceptor;

import java.time.Duration;

@Configuration
@ConfigurationProperties("chat.service")
@Setter
@Getter
public class ChatServiceConfiguration {
    private String url;
    private Duration connectionTimeout;

    @Bean
    public RestClient chatRestClient(LogbookClientHttpRequestInterceptor interceptor) {
        return RestClient.builder()
                .baseUrl(url)
                .requestInterceptor(interceptor)
                .requestFactory(getClientHttpRequestFactory())
                .build();
    }

    @Bean
    public ChatService chatClient(RestClient chatRestClient) {
        HttpServiceProxyFactory httpServiceProxyFactory =
                HttpServiceProxyFactory.builderFor(RestClientAdapter.create(chatRestClient)).build();
        return httpServiceProxyFactory.createClient(ChatService.class);
    }

    private ClientHttpRequestFactory getClientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory =
                new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectionRequestTimeout(connectionTimeout);
        return clientHttpRequestFactory;
    }
}
