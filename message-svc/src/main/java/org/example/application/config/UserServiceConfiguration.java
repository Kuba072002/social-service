package org.example.application.config;

import lombok.Getter;
import lombok.Setter;
import org.example.domain.user.UserService;
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
@ConfigurationProperties("user.service")
@Setter
@Getter
public class UserServiceConfiguration {
    private String url;
    private Duration connectionTimeout;

    @Bean
    public RestClient userRestClient(LogbookClientHttpRequestInterceptor interceptor) {
        return RestClient.builder()
                .baseUrl(url)
                .requestInterceptor(interceptor)
                .requestFactory(getClientHttpRequestFactory())
                .build();
    }

    @Bean
    public UserService userClient(RestClient userRestClient) {
        HttpServiceProxyFactory httpServiceProxyFactory =
                HttpServiceProxyFactory.builderFor(RestClientAdapter.create(userRestClient)).build();
        return httpServiceProxyFactory.createClient(UserService.class);
    }

    private ClientHttpRequestFactory getClientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory =
                new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectionRequestTimeout(connectionTimeout);
        return clientHttpRequestFactory;
    }
}
