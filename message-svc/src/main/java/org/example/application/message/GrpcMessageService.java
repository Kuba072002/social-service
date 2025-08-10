package org.example.application.message;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.example.dto.message.MessageDTO;
import org.example.dto.message.MessageRequest;
import org.example.grpc.CreateMessageResponse;
import org.example.grpc.GetMessagesResponse;
import org.example.grpc.MessageServiceGrpc;

@GrpcService
@RequiredArgsConstructor
public class GrpcMessageService extends MessageServiceGrpc.MessageServiceImplBase {
    private final MessageService messageService;

    @Override
    public void getMessages(org.example.grpc.GetMessagesRequest request, StreamObserver<org.example.grpc.GetMessagesResponse> responseObserver) {
        var messages = messageService.getMessages(
                request.getSenderId(),
                request.getChatId(),
                null,
                null,
                request.getLimit()
        );
        GetMessagesResponse.Builder responseBuilder = GetMessagesResponse.newBuilder();
        for (MessageDTO dto : messages) {
            responseBuilder.addMessages(
                    org.example.grpc.Message.newBuilder()
                            .setChatId(dto.chatId())
                            .setMessageId(dto.messageId().toString())
                            .setSenderId(dto.senderId())
                            .setContent(dto.content() == null ? "" : dto.content())
//                            .setMediaContent(dto.mediaContent() == null ? "" : dto.mediaContent())
//                            .setCreatedAt(dto.createdAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                            .build()
            );
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void createMessage(org.example.grpc.CreateMessageRequest request, StreamObserver<org.example.grpc.CreateMessageResponse> responseObserver) {
        MessageRequest messageRequest = new MessageRequest(request.getChatId(), request.getContent());
        var messageId = messageService.createMessage(request.getSenderId(), messageRequest);
        responseObserver.onNext(CreateMessageResponse.newBuilder().setMessageId(messageId.toString()).build());
        responseObserver.onCompleted();
    }
}
