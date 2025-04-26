package org.example.application.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Value("${stomp.rabbitmq.port:61613}")
    private String rabbitmqPort;
    @Value("${spring.rabbitmq.virtual-host:my_vhost}")
    private String virtualHost;
    @Value("${spring.rabbitmq.username:rabbit}")
    private String rabbitmqUsername;
    @Value("${spring.rabbitmq.password:rabbit}")
    private String rabbitmqPassword;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableStompBrokerRelay("/topic", "/queue", "/exchange", "/amq/queue")
                .setRelayPort(Integer.parseInt(rabbitmqPort))
                .setVirtualHost(virtualHost)
                .setClientLogin(rabbitmqUsername)
                .setClientPasscode(rabbitmqPassword)
                .setSystemLogin(rabbitmqUsername)
                .setSystemPasscode(rabbitmqPassword);
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
