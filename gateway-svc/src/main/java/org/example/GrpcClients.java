package org.example;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.example.grpc.MessageServiceGrpc;
import org.example.grpc.UserServiceGrpc;
import org.example.grpc.chat.ChatServiceGrpc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GrpcClients {
    private final ManagedChannel userChannel;
    private final UserServiceGrpc.UserServiceBlockingStub userStub;

    private final ManagedChannel chatChannel;
    private final ChatServiceGrpc.ChatServiceBlockingStub chatStub;

    private final ManagedChannel messageChannel;
    private final MessageServiceGrpc.MessageServiceBlockingStub messageStub;

    public GrpcClients(@Value(value = "${user.service.url.grpc}") String userHost,@Value(value = "${user.service.port.grpc}") int userPort,
                       @Value(value = "${chat.service.url.grpc}") String chatHost,@Value(value = "${chat.service.port.grpc}") int chatPort,
                       @Value(value = "${message.service.url.grpc}")String messageHost,@Value(value = "${message.service.port.grpc}") int messagePort) {
        this.userChannel = ManagedChannelBuilder.forAddress(userHost, userPort)
                .usePlaintext()  // Remove or replace with TLS in production
                .build();
        this.userStub = UserServiceGrpc.newBlockingStub(userChannel);

        this.chatChannel = ManagedChannelBuilder.forAddress(chatHost, chatPort)
                .usePlaintext()
                .build();
        this.chatStub = ChatServiceGrpc.newBlockingStub(chatChannel);

        this.messageChannel = ManagedChannelBuilder.forAddress(messageHost, messagePort)
                .usePlaintext()
                .build();
        this.messageStub = MessageServiceGrpc.newBlockingStub(messageChannel);
    }

    public UserServiceGrpc.UserServiceBlockingStub getUserStub() {
        return userStub;
    }

    public ChatServiceGrpc.ChatServiceBlockingStub getChatStub() {
        return chatStub;
    }

    public MessageServiceGrpc.MessageServiceBlockingStub getMessageStub() {
        return messageStub;
    }

    public void shutdown() throws InterruptedException {
        userChannel.shutdown().awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
        chatChannel.shutdown().awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
        messageChannel.shutdown().awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
    }
}


