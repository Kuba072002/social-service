package org.example.application.chat;

import com.google.protobuf.StringValue;
import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.example.application.chat.service.CreateChatService;
import org.example.application.chat.service.DeleteChatService;
import org.example.application.chat.service.GetChatService;
import org.example.application.chat.service.ModifyChatService;
import org.example.grpc.chat.*;

import java.util.HashSet;
import java.util.Set;

@GrpcService
@RequiredArgsConstructor
public class GrpcChatService
        extends ChatServiceGrpc.ChatServiceImplBase {
    private final CreateChatService createChatService;
    private final ModifyChatService modifyChatService;
    private final GetChatService getChatService;
    private final DeleteChatService deleteChatService;

    @Override
    public void modifyChatParticipants(ModifyChatParticipantsRequestWithIds request, StreamObserver<Empty> responseObserver) {
        Set<Long> userIdsToDelete = new HashSet<>(request.getModifyChatParticipantsRequest().getUserIdsToDeleteList());
        Set<Long> userIdsToAdd = new HashSet<>(request.getModifyChatParticipantsRequest().getUserIdsToAddList());
        org.example.dto.chat.ModifyChatParticipantsRequest modifyRequest = new org.example.dto.chat.ModifyChatParticipantsRequest(
                userIdsToAdd, userIdsToDelete
        );
        modifyChatService.modifyChatParticipants(request.getUserId(), request.getChatId(), modifyRequest);
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void modifyChat(ModifyChatRequestWithIds request, StreamObserver<Empty> responseObserver) {
        org.example.dto.chat.ModifyChatRequest modifyChatRequest = new org.example.dto.chat.ModifyChatRequest(
                request.getModifyChatRequest().getName(),
                request.getModifyChatRequest().getImageUrl()
        );
        modifyChatService.modifyChat(request.getUserId(), request.getChatId(), modifyChatRequest);
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void getUserChats(GetUserChatsRequest request, StreamObserver<GetUserChatsResponse> responseObserver) {
        var chats = getChatService.getChats(request.getUserId(), request.getIsPrivate().getValue(), request.getPageNumber().getValue(), request.getPageSize().getValue());
        var response = chats.stream()
                .map(c ->
                        ChatDetail.newBuilder()
                                .setChatId(c.getChatId())
                                .setName(StringValue.of(c.getName()))
                                .setImageUrl(StringValue.of(c.getImageUrl()))
                                .setIsPrivate(c.getPrivate())
                                .setLastMessageAt(Timestamp.newBuilder().setSeconds(c.getLastMessageAt().toEpochSecond()).setNanos(c.getLastMessageAt().getNano())
                                        .build())
                                .setLastReadAt(Timestamp.newBuilder().setSeconds(c.getLastReadAt().toEpochSecond()).setNanos(c.getLastReadAt().getNano())
                                        .build())
                                .build()
                ).toList();
        responseObserver.onNext(GetUserChatsResponse.newBuilder()
                .addAllChats(response)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void getChatParticipants(GetChatParticipantsRequest request, StreamObserver<GetChatParticipantsResponse> responseObserver) {
        var participants = getChatService.getParticipants(request.getUserId(), request.getChatId());
        var response = participants.stream()
                .map(p -> {
                    var builder = ParticipantDTO.newBuilder();
                    if (p.userId() != null) builder.setUserId(p.userId());
                    if (p.imageUrl() != null) builder.setImageUrl(p.imageUrl());
                    if (p.joinedAt() != null) builder.setJoinedAt(Timestamp.newBuilder()
                            .setSeconds(p.joinedAt().getSecond())
                            .setNanos(p.joinedAt().getNano()));
                    return builder.build();
                })
                .toList();
        responseObserver.onNext(GetChatParticipantsResponse.newBuilder()
                .addAllParticipants(response)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteChat(ChatIdRequest request, StreamObserver<Empty> responseObserver) {
        deleteChatService.delete(request.getUserId(), request.getChatId());
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void createChat(ChatRequest request, StreamObserver<CreateChatResponse> responseObserver) {
        var id = createChatService.create(request.getUserId(), new org.example.dto.chat.ChatRequest(
                request.getName(), request.getImageUrl(), request.getIsPrivate(), new HashSet<>(request.getUserIdsList())
        ));
        responseObserver.onNext(CreateChatResponse.newBuilder().setChatId(id).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getChatParticipantIds(ParticipantIdsRequest request, StreamObserver<GetChatParticipantIdsResponse> responseObserver) {
        var response = getChatService.getParticipantIds(request.getChatId());
        responseObserver.onNext(GetChatParticipantIdsResponse.newBuilder().addAllParticipants(response).build());
        responseObserver.onCompleted();
    }
}
