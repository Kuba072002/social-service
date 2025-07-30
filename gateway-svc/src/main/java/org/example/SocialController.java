package org.example;

import lombok.RequiredArgsConstructor;
import org.example.dto.chat.*;
import org.example.dto.message.MessageDTO;
import org.example.dto.message.MessageEditRequest;
import org.example.dto.message.MessageRequest;
import org.example.dto.user.SignInRequest;
import org.example.dto.user.SignInResponse;
import org.example.dto.user.SignUpRequest;
import org.example.dto.user.UserDTO;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class SocialController {
    private final WebClient userSvcClient;
    private final WebClient chatSvcClient;
    private final WebClient messageSvcClient;
    private final AuthenticationService authenticationService;
    private static final String USER_ID_HEADER = "userId";
    private static final String AUTH_HEADER = "authHeader";

    //user-svc
    @MutationMapping("register")
    public Mono<Long> register(@Argument SignUpRequest signUpRequest) {
        return userSvcClient.post()
                .uri("/register")
                .bodyValue(signUpRequest)
                .retrieve()
                .bodyToMono(Long.class);
    }

    @MutationMapping("login")
    public Mono<SignInResponse> login(@Argument SignInRequest signInRequest) {
        return userSvcClient.post()
                .uri("/login")
                .bodyValue(signInRequest)
                .retrieve()
                .bodyToMono(SignInResponse.class);
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
        return authenticationService.getUserId(authHeader)
                .delayUntil(this::validateUser)
                .flatMap(userId -> chatSvcClient.post()
                        .uri("/chats")
                        .header(USER_ID_HEADER, String.valueOf(userId))
                        .bodyValue(chatRequest)
                        .retrieve()
                        .bodyToMono(Long.class));
    }

    @MutationMapping("modifyChat")
    public Mono<Boolean> modifyChat(
            @Argument Long chatId,
            @Argument ModifyChatRequest modifyChatRequest,
            @ContextValue(name = AUTH_HEADER) String authHeader
    ) {
        return authenticationService.getUserId(authHeader)
                .delayUntil(this::validateUser)
                .flatMap(userId -> chatSvcClient.put()
                        .uri("/chats/{chatId}", chatId)
                        .header(USER_ID_HEADER, String.valueOf(userId))
                        .bodyValue(modifyChatRequest)
                        .retrieve()
                        .bodyToMono(Void.class))
                .thenReturn(true);
    }

    @MutationMapping("modifyChatParticipants")
    public Mono<Boolean> modifyChatParticipants(
            @Argument Long chatId,
            @Argument ModifyChatParticipantsRequest modifyChatParticipantsRequest,
            @ContextValue(name = AUTH_HEADER) String authHeader
    ) {
        return authenticationService.getUserId(authHeader)
                .delayUntil(this::validateUser)
                .flatMap(userId -> chatSvcClient.put()
                        .uri("/chats/{chatId}/participants", chatId)
                        .header(USER_ID_HEADER, String.valueOf(userId))
                        .bodyValue(modifyChatParticipantsRequest)
                        .retrieve()
                        .bodyToMono(Void.class))
                .thenReturn(true);
    }

    @MutationMapping("updateLastReadAt")
    public Mono<Boolean> updateLastReadAt(
            @Argument Long chatId,
            @Argument UpdateChatReadAtRequest request,
            @ContextValue(name = AUTH_HEADER) String authHeader
    ) {
        return authenticationService.getUserId(authHeader)
                .delayUntil(this::validateUser)
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
        return authenticationService.getUserId(authHeader)
                .delayUntil(this::validateUser)
                .flatMap(userId -> chatSvcClient.delete()
                        .uri("/chats/{chatId}", chatId)
                        .header(USER_ID_HEADER, String.valueOf(userId))
                        .retrieve()
                        .bodyToMono(Void.class))
                .thenReturn(true);
    }

    @MutationMapping("deleteParticipant")
    public Mono<Boolean> deleteParticipant(@Argument Long chatId, @ContextValue(name = AUTH_HEADER) String authHeader) {
        return authenticationService.getUserId(authHeader)
                .delayUntil(this::validateUser)
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
        String additionalParams = "";
        if (pageNumber != null) additionalParams += "&pageNumber=" + pageNumber;
        if (pageSize != null) additionalParams += "&pageSize=" + pageSize;
        String finalAdditionalParams = additionalParams;
        return authenticationService.getUserId(authHeader)
                .delayUntil(this::validateUser)
                .flatMapMany(userId -> chatSvcClient.get()
                        .uri("/chats?isPrivate=" + isPrivate + finalAdditionalParams)
                        .header(USER_ID_HEADER, String.valueOf(userId))
                        .retrieve()
                        .bodyToFlux(ChatDetail.class));
    }

    @QueryMapping("getChatParticipants")
    public Flux<ParticipantDTO> getChatParticipants(@Argument Long chatId, @ContextValue(name = AUTH_HEADER) String authHeader) {
        return authenticationService.getUserId(authHeader)
                .delayUntil(this::validateUser)
                .flatMapMany(userId -> chatSvcClient.get()
                        .uri("/chats/{chatId}/participants", chatId)
                        .header(USER_ID_HEADER, String.valueOf(userId))
                        .retrieve()
                        .bodyToFlux(ParticipantDTO.class));
    }

    //message-svc
    @MutationMapping("createMessage")
    public Mono<UUID> createMessage(@Argument MessageRequest messageRequest, @ContextValue(name = AUTH_HEADER) String authHeader) {
        return authenticationService.getUserId(authHeader)
                .delayUntil(this::validateUser)
                .flatMap(userId -> messageSvcClient.post()
                        .uri("/messages")
                        .header(USER_ID_HEADER, String.valueOf(userId))
                        .bodyValue(messageRequest)
                        .retrieve()
                        .bodyToMono(UUID.class));
    }

    @MutationMapping("editMessage")
    public Mono<Boolean> editMessage(@Argument MessageEditRequest messageEditRequest, @ContextValue(name = AUTH_HEADER) String authHeader) {
        return authenticationService.getUserId(authHeader)
                .delayUntil(this::validateUser)
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
                .delayUntil(this::validateUser)
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
        String additionalParams = "";
        if (from != null) additionalParams += "&from=" + from;
        if (to != null) additionalParams += "&to=" + to;
        if (limit != null) additionalParams += "&limit=" + limit;
        String finalAdditionalParams = additionalParams;
        return authenticationService.getUserId(authHeader)
                .flatMapMany(userId -> messageSvcClient.get()
                        .uri("/messages?chatId=" + chatId + finalAdditionalParams)
                        .header(USER_ID_HEADER, String.valueOf(userId))
                        .retrieve()
                        .bodyToFlux(MessageDTO.class));
    }

    private Mono<Void> validateUser(Long userId) {
        return userSvcClient.get()
                .uri("/internal/users/{userId}", userId)
                .retrieve()
                .bodyToMono(UserDTO.class)
                .then();
    }
}
