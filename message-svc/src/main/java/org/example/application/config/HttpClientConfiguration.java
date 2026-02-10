package org.example.application.config;

import org.example.domain.chat.ChatService;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;
import org.zalando.logbook.spring.LogbookClientHttpRequestInterceptor;

@Configuration(proxyBeanMethods = false)
@ImportHttpServices(ChatService.class)
public class HttpClientConfiguration {
    @Bean
    RestClientCustomizer userAgentCustomizer(LogbookClientHttpRequestInterceptor interceptor) {
        return restClientBuilder -> restClientBuilder
                .defaultHeader("User-Agent", "message-service")
                .requestInterceptor(interceptor);
    }
}
