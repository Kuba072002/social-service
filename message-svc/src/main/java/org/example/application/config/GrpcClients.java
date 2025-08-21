package org.example.application.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.Getter;
import org.example.grpc.chat.ChatServiceGrpc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class GrpcClients {
    private final ManagedChannel chatChanel;
    private final ChatServiceGrpc.ChatServiceBlockingStub chatStub;

    public GrpcClients(
            @Value(value = "${chat.service.url.grpc}") String chatHost,
            @Value(value = "${chat.service.port.grpc}") int chatPort,
            @Value(value = "${grpc.development.mode}") boolean developmentMode
    ) {
        var channelBuilder = ManagedChannelBuilder.forAddress(chatHost, chatPort);
        if (developmentMode) {
            channelBuilder.usePlaintext();
        }
        this.chatChanel = channelBuilder.build();
        this.chatStub = ChatServiceGrpc.newBlockingStub(chatChanel);
    }
}
