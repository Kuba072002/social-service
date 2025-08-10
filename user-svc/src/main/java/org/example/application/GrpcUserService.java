package org.example.application;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.example.application.service.AuthService;
import org.example.application.service.CreateUserService;
import org.example.domain.service.UserService;
import org.example.grpc.*;

import java.util.HashSet;
import java.util.stream.Collectors;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class GrpcUserService extends UserServiceGrpc.UserServiceImplBase {
    private final CreateUserService createUserService;
    private final UserService userService;
    private final AuthService authService;

    @Override
    public void findUsers(FindUsersRequest request, StreamObserver<FindUsersResponse> responseObserver) {
        var users = userService.findByIds(new HashSet<>(request.getUserIdsList()));
        var response = users.stream()
                .map(u -> {
                    var builder = org.example.grpc.UserDTO.newBuilder()
                            .setUserName(u.getUserName())
                            .setEmail(u.getEmail())
                            .setId(u.getId());
                    if (u.getImageUrl() != null) builder.setImageUrl(u.getImageUrl());
                    return builder.build();
                }).toList();
        responseObserver.onNext(FindUsersResponse.newBuilder()
                .addAllUsers(response)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void login(SignInRequest request, StreamObserver<SignInResponse> responseObserver) {
        log.info("Try to login");
        log.info(request.toString());
        org.example.dto.user.SignInResponse restResponse =
                authService.authUser(request.getEmail(), request.getPassword());

        UserDTO grpcUser = UserDTO.newBuilder()
                .setId(restResponse.user().id())
                .setUserName(restResponse.user().userName())
                .setEmail(restResponse.user().email())
                .setImageUrl(restResponse.user().imageUrl() == null ? "" : restResponse.user().imageUrl())
                .build();

        // Build gRPC response
        SignInResponse grpcResponse = SignInResponse.newBuilder()
                .setUser(grpcUser)
                .setToken(restResponse.token())
                .build();

        responseObserver.onNext(grpcResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void register(SignUpRequest request, StreamObserver<SignUpResponse> responseObserver) {
        log.info("Try to register");
        log.info(request.toString());
        org.example.dto.user.SignUpRequest restRequest = new org.example.dto.user.SignUpRequest(
                request.getUserName(),
                request.getEmail(),
                request.getPassword(),
                request.getConfirmPassword()
        );
        Long userId = createUserService.createUser(restRequest);

        SignUpResponse response = SignUpResponse.newBuilder()
                .setUserId(userId)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
