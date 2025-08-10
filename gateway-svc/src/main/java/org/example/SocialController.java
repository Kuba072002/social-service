package org.example;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Int32Value;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.chat.*;
import org.example.dto.chat.ChatDetail;
import org.example.dto.chat.ChatRequest;
import org.example.dto.chat.ModifyChatParticipantsRequest;
import org.example.dto.chat.ModifyChatRequest;
import org.example.dto.chat.ParticipantDTO;
import org.example.dto.message.MessageDTO;
import org.example.dto.message.MessageEditRequest;
import org.example.dto.message.MessageRequest;
import org.example.dto.user.SignInRequest;
import org.example.dto.user.SignInResponse;
import org.example.dto.user.SignUpRequest;
import org.example.dto.user.UserDTO;
import org.example.grpc.CreateMessageRequest;
import org.example.grpc.chat.*;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

import static org.example.GatewayApplicationError.INVALID_RESPONSE;

@Controller
@RequiredArgsConstructor
@Slf4j
public class SocialController {
    private final WebClient userSvcClient;
    private final WebClient chatSvcClient;
    private final WebClient messageSvcClient;
    private final GrpcClients grpcClients;
    private final AuthenticationService authenticationService;
    private static final String USER_ID_HEADER = "userId";
    private static final String AUTH_HEADER = "authHeader";

    //user-svc
    @MutationMapping("register")
    public Mono<Long> register(@Argument SignUpRequest signUpRequest) {
//        log.info("try to register");
//        log.info(signUpRequest.toString());
        var request = org.example.grpc.SignUpRequest.newBuilder()
                .setUserName(signUpRequest.userName())
                .setEmail(signUpRequest.email())
                .setPassword(signUpRequest.password())
                .setConfirmPassword(signUpRequest.confirmPassword())
                .build();

//        log.info(request.toString());
        try {
            var response = grpcClients.getUserStub().register(request);
            return Mono.just(response.getUserId());
        } catch (Exception e) {
            log.info(e.getMessage(), e);
            return Mono.error(new ApplicationException(INVALID_RESPONSE));
        }
    }

    @MutationMapping("login")
    public Mono<SignInResponse> login(@Argument SignInRequest signInRequest) {
        try {

            var response = grpcClients.getUserStub().login(org.example.grpc.SignInRequest.newBuilder()
                    .setEmail(signInRequest.email())
                    .setPassword(signInRequest.password())
                    .build());
            return Mono.just(new SignInResponse(new UserDTO(
                    response.getUser().getId(),
                    response.getUser().getUserName(),
                    response.getUser().getEmail(),
                    response.getUser().getImageUrl()
            ), response.getToken()));
        } catch (Exception e) {
            return Mono.error(new ApplicationException(INVALID_RESPONSE));
        }
    }

    @QueryMapping("users")
    public Flux<UserDTO> users(@Argument String userName) {
        return userSvcClient.get()
                .uri("/login?username=" + userName)
                .retrieve()
                .bodyToFlux(UserDTO.class);
    }

    //chat-svc
    @MutationMapping("createChat")
    public Mono<Long> createChat(@Argument ChatRequest chatRequest, @ContextValue(name = AUTH_HEADER) String authHeader) {
        try {
            return authenticationService.getUserId(authHeader)
                    .map(userId -> {
                        var response = grpcClients.getChatStub().createChat(
                                org.example.grpc.chat.ChatRequest.newBuilder()
                                        .setUserId(userId)
                                        .setName(chatRequest.name())
                                        .setImageUrl(chatRequest.imageUrl())
                                        .setIsPrivate(chatRequest.isPrivate())
                                        .addAllUserIds(chatRequest.userIds())
                                        .build()
                        );
                        return response.getChatId();
                    });
        } catch (Exception e) {
            return Mono.error(new ApplicationException(INVALID_RESPONSE));
        }
    }

    @MutationMapping("modifyChat")
    public Mono<Boolean> modifyChat(
            @Argument Long chatId,
            @Argument ModifyChatRequest modifyChatRequest,
            @ContextValue(name = AUTH_HEADER) String authHeader
    ) {
        try {
            return authenticationService.getUserId(authHeader)
                    .map(userId -> grpcClients.getChatStub().modifyChat(
                            ModifyChatRequestWithIds.newBuilder()
                                    .setChatId(chatId)
                                    .setUserId(userId)
                                    .setModifyChatRequest(org.example.grpc.chat.ModifyChatRequest.newBuilder()
                                            .setName(modifyChatRequest.name())
                                            .setImageUrl(modifyChatRequest.imageUrl())
                                            .build())
                                    .build()
                    ))
                    .thenReturn(true);
        } catch (Exception e) {
            return Mono.error(new ApplicationException(INVALID_RESPONSE));
        }
    }

    @MutationMapping("modifyChatParticipants")
    public Mono<Boolean> modifyChatParticipants(
            @Argument Long chatId,
            @Argument ModifyChatParticipantsRequest modifyChatParticipants,
            @ContextValue(name = AUTH_HEADER) String authHeader
    ) {
        try {
            return authenticationService.getUserId(authHeader)
                    .map(userId -> grpcClients.getChatStub().modifyChatParticipants(
                                    ModifyChatParticipantsRequestWithIds.newBuilder()
                                            .setUserId(userId)
                                            .setChatId(chatId)
                                            .setModifyChatParticipantsRequest(org.example.grpc.chat.ModifyChatParticipantsRequest.newBuilder()
                                                    .addAllUserIdsToAdd(modifyChatParticipants.userIdsToAdd())
                                                    .addAllUserIdsToDelete(modifyChatParticipants.userIdsToDelete())
                                                    .build())
                                            .build()
                            )
                    )
                    .thenReturn(true);

        } catch (Exception e) {
            return Mono.error(new ApplicationException(INVALID_RESPONSE));
        }
    }

    @MutationMapping("updateLastReadAt")
    public Mono<Boolean> updateLastReadAt(
            @Argument Long chatId,
            @Argument UpdateChatReadAtRequest request,
            @ContextValue(name = AUTH_HEADER) String authHeader
    ) {
        return authenticationService.getUserId(authHeader)
                .flatMap(userId -> chatSvcClient.put()
                        .uri("/chats/{chatId}/participants/last_read_at", chatId)
                        .header(USER_ID_HEADER, String.valueOf(userId))
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(Void.class))
                .thenReturn(true);
    }

    @MutationMapping("deleteChat")
    public Mono<Boolean> deleteChat(@Argument Long chatId, @ContextValue(name = AUTH_HEADER) String authHeader) {
        try {
            return authenticationService.getUserId(authHeader)
                    .map(userId -> grpcClients.getChatStub().deleteChat(ChatIdRequest.newBuilder()
                            .setUserId(userId)
                            .setChatId(chatId)
                            .build()))
                    .thenReturn(true);
        } catch (Exception e) {
            return Mono.error(new ApplicationException(INVALID_RESPONSE));
        }
    }

    @MutationMapping("deleteParticipant")
    public Mono<Boolean> deleteParticipant(@Argument Long chatId, @ContextValue(name = AUTH_HEADER) String authHeader) {
        return authenticationService.getUserId(authHeader)
                .flatMap(userId -> chatSvcClient.delete()
                        .uri("/chats/{chatId}/participants", chatId)
                        .header(USER_ID_HEADER, String.valueOf(userId))
                        .retrieve()
                        .bodyToMono(Void.class))
                .thenReturn(true);
    }

    @QueryMapping("getUserChats")
    public Flux<ChatDetail> getUserChats(
            @Argument Boolean isPrivate,
            @Argument Integer pageNumber,
            @Argument Integer pageSize,
            @ContextValue(name = AUTH_HEADER) String authHeader
    ) {
        try {
            String additionalParams = "";
            if (pageNumber != null) additionalParams += "&pageNumber=" + pageNumber;
            if (pageSize != null) additionalParams += "&pageSize=" + pageSize;
            String finalAdditionalParams = additionalParams;
            return authenticationService.getUserId(authHeader)
                    .flatMapMany(userId -> Flux.fromIterable(grpcClients.getChatStub().getUserChats(
                                    GetUserChatsRequest.newBuilder()
                                            .setUserId(userId)
                                            .setIsPrivate(BoolValue.of(isPrivate))
                                            .setPageNumber(pageNumber != null ? Int32Value.of(pageNumber) : null)
                                            .setPageSize(pageSize != null ? Int32Value.of(pageSize) : null)
                                            .build()
                            ).getChatsList()
                    )).map(c -> new ChatDetail(
                            c.getChatId(),
                            c.getName().toString(),
                            c.getImageUrl().toString(),
                            c.getIsPrivate(),
                            Instant.ofEpochSecond(
                                    c.getLastMessageAt().getSeconds(),
                                    c.getLastMessageAt().getNanos()
                            ),
                            Instant.ofEpochSecond(
                                    c.getLastReadAt().getSeconds(),
                                    c.getLastReadAt().getNanos()
                            ),
                            c.getOtherUser().getValue()
                    ));
        } catch (Exception e) {
            throw new ApplicationException(INVALID_RESPONSE);
        }
    }

    @QueryMapping("getChatParticipants")
    public Flux<ParticipantDTO> getChatParticipants(@Argument Long chatId, @ContextValue(name = AUTH_HEADER) String authHeader) {
        try {
            return authenticationService.getUserId(authHeader)
                    .flatMapMany(userId -> Flux.fromIterable(grpcClients.getChatStub().getChatParticipants(
                            GetChatParticipantsRequest.newBuilder().setUserId(userId).setChatId(chatId).build()
                    ).getParticipantsList()))
                    .map(p -> new ParticipantDTO(
                            p.getUserId(),
                            p.getUserName(), p.getImageUrl(), p.getRole(),
                            OffsetDateTime.ofInstant(
                                    Instant.ofEpochSecond(p.getJoinedAt().getSeconds(), p.getJoinedAt().getNanos()), ZoneId.systemDefault())
                    ));
        } catch (Exception e) {
            throw new ApplicationException(INVALID_RESPONSE);
        }
    }

    //message-svc
    @MutationMapping("createMessage")
    public Mono<UUID> createMessage(@Argument MessageRequest messageRequest, @ContextValue(name = AUTH_HEADER) String authHeader) {
        try {
            return authenticationService.getUserId(authHeader)
                    .map(userId -> grpcClients.getMessageStub().createMessage(
                                    CreateMessageRequest.newBuilder()
                                            .setSenderId(userId)
                                            .setChatId(messageRequest.chatId())
                                            .setContent(messageRequest.content())
                                            .build()
                            ).getMessageId()
                    ).map(UUID::fromString);
        } catch (Exception e) {
            return Mono.error(new ApplicationException(INVALID_RESPONSE));
        }
    }

    @MutationMapping("editMessage")
    public Mono<Boolean> editMessage(@Argument MessageEditRequest messageEditRequest, @ContextValue(name = AUTH_HEADER) String authHeader) {
        return authenticationService.getUserId(authHeader)
                .flatMap(userId -> messageSvcClient.put()
                        .uri("/messages")
                        .header(USER_ID_HEADER, String.valueOf(userId))
                        .bodyValue(messageEditRequest)
                        .retrieve()
                        .bodyToMono(Void.class))
                .thenReturn(true);
    }

    @MutationMapping("deleteMessage")
    public Mono<Boolean> deleteMessage(@Argument Long chatId, @Argument UUID messageId, @ContextValue(name = AUTH_HEADER) String authHeader) {
        return authenticationService.getUserId(authHeader)
                .flatMap(userId -> messageSvcClient.delete()
                        .uri("/messages?chatId=" + chatId + "&messageId=" + messageId)
                        .header(USER_ID_HEADER, String.valueOf(userId))
                        .retrieve()
                        .bodyToMono(Void.class))
                .thenReturn(true);
    }

    @QueryMapping("getMessages")
    public Flux<MessageDTO> getMessages(
            @Argument Long chatId,
            @Argument OffsetDateTime from,
            @Argument OffsetDateTime to,
            @Argument Integer limit,
            @ContextValue(name = AUTH_HEADER) String authHeader
    ) {
        try {
            String additionalParams = "";
            if (limit == null) limit = 50;
            Integer finalLimit = limit;
            return authenticationService.getUserId(authHeader)
                    .flatMapMany(userId -> Flux.fromIterable(grpcClients.getMessageStub().getMessages(
                            org.example.grpc.GetMessagesRequest.newBuilder()
                                    .setSenderId(userId)
                                    .setChatId(chatId)
                                    .setLimit(finalLimit)
                                    .build()
                    ).getMessagesList()))
                    .map(m -> new MessageDTO(
                            m.getChatId(),
                            UUID.fromString(m.getMessageId()),
                            m.getSenderId(),
                            m.getContent(),
                            m.getMediaContent(),
                            OffsetDateTime.ofInstant(
                                    Instant.now(), ZoneId.systemDefault()
                            )
                    ));
        } catch (Exception e) {
            throw new ApplicationException(INVALID_RESPONSE);
        }
    }
}
