package org.example.application.config;

import lombok.RequiredArgsConstructor;
import org.example.application.interceptor.StompAuthInterceptor;
import org.example.application.interceptor.UserHandshakeHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Value("${spring.rabbitmq.host:localhost}")
    private String relayHost;
    @Value("${stomp.rabbitmq.port:61613}")
    private String rabbitmqPort;
    @Value("${spring.rabbitmq.virtual-host:my_vhost}")
    private String virtualHost;
    @Value("${spring.rabbitmq.username:rabbit}")
    private String rabbitmqUsername;
    @Value("${spring.rabbitmq.password:rabbit}")
    private String rabbitmqPassword;

    private final StompAuthInterceptor stompAuthInterceptor;
    private final UserHandshakeHandler userHandshakeHandler;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost(relayHost)
                .setRelayPort(Integer.parseInt(rabbitmqPort))
                .setVirtualHost(virtualHost)
                .setClientLogin(rabbitmqUsername)
                .setClientPasscode(rabbitmqPassword)
                .setSystemLogin(rabbitmqUsername)
                .setSystemPasscode(rabbitmqPassword);
//        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setHandshakeHandler(userHandshakeHandler)
                .setAllowedOriginPatterns("*")
                .withSockJS()
                .setHeartbeatTime(25_000)
                .setDisconnectDelay(5_000);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthInterceptor);
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthInterceptor);
    }
}
