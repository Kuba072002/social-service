package org.example.application.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.Getter;
import org.example.grpc.UserServiceGrpc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class GrpcClients {
    private final ManagedChannel userChannel;
    private final UserServiceGrpc.UserServiceBlockingStub userStub;

    public GrpcClients(
            @Value(value = "${user.service.url.grpc}") String userHost,
            @Value(value = "${user.service.port.grpc}") int userPort,
            @Value(value = "${grpc.development.mode}") boolean developmentMode
        ) {
        var chanelBuilder = ManagedChannelBuilder.forAddress(userHost, userPort);
        if (developmentMode) chanelBuilder.usePlaintext();
        this.userChannel = chanelBuilder.build();
        this.userStub = UserServiceGrpc.newBlockingStub(userChannel);
    }
}
