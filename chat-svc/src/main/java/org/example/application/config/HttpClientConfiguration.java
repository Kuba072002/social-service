package org.example.application.config;

import org.example.domain.message.MessageService;
import org.example.domain.user.UserService;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;
import org.zalando.logbook.spring.LogbookClientHttpRequestInterceptor;

@Configuration(proxyBeanMethods = false)
@ImportHttpServices(UserService.class)
@ImportHttpServices(MessageService.class)
public class HttpClientConfiguration {
    @Bean
    RestClientCustomizer userAgentCustomizer(LogbookClientHttpRequestInterceptor interceptor) {
        return restClientBuilder -> restClientBuilder
                .defaultHeader("User-Agent", "chat-service")
                .requestInterceptor(interceptor);
    }
}
