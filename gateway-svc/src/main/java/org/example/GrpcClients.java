package org.example;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.Getter;
import org.example.grpc.MessageServiceGrpc;
import org.example.grpc.UserServiceGrpc;
import org.example.grpc.chat.ChatServiceGrpc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Getter
public class GrpcClients {
    private final ManagedChannel userChannel;
    private final UserServiceGrpc.UserServiceBlockingStub userStub;

    private final ManagedChannel chatChannel;
    private final ChatServiceGrpc.ChatServiceBlockingStub chatStub;

    private final ManagedChannel messageChannel;
    private final MessageServiceGrpc.MessageServiceBlockingStub messageStub;

    public GrpcClients(
            @Value(value = "${user.service.url.grpc}") String userHost, @Value(value = "${user.service.port.grpc}") int userPort,
            @Value(value = "${chat.service.url.grpc}") String chatHost, @Value(value = "${chat.service.port.grpc}") int chatPort,
            @Value(value = "${message.service.url.grpc}") String messageHost, @Value(value = "${message.service.port.grpc}") int messagePort,
            @Value(value = "${grpc.development.mode}") boolean developmentMode
    ) {
        var channelBuilders = Map.of(
                "user", ManagedChannelBuilder.forAddress(userHost, userPort),
                "chat", ManagedChannelBuilder.forAddress(chatHost, chatPort),
                "message", ManagedChannelBuilder.forAddress(messageHost, messagePort)
        );
        if (developmentMode) {
            channelBuilders.values().forEach(ManagedChannelBuilder::usePlaintext);
        }
        this.userChannel = channelBuilders.get("user").build();
        this.userStub = UserServiceGrpc.newBlockingStub(userChannel);

        this.chatChannel = channelBuilders.get("chat").build();
        this.chatStub = ChatServiceGrpc.newBlockingStub(chatChannel);

        this.messageChannel = channelBuilders.get("message").build();
        this.messageStub = MessageServiceGrpc.newBlockingStub(messageChannel);
    }

    public void shutdown() throws InterruptedException {
        userChannel.shutdown().awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
        chatChannel.shutdown().awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
        messageChannel.shutdown().awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
    }
}


