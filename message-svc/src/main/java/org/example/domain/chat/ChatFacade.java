package org.example.domain.chat;

import lombok.RequiredArgsConstructor;
import org.example.application.config.GrpcClients;
import org.example.grpc.chat.ParticipantIdsRequest;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ChatFacade {
    private final ChatRepository chatRepository;
    private final GrpcClients grpcClients;

    public Set<Long> findChatParticipants(Long chatId) {
        return chatRepository.findById(chatId)
                .map(chat -> chat.getUsers().getIds())
                .orElseGet(() -> new HashSet<>(fetchChatParticipants(chatId)));
    }

    public void save(Long chatId, Set<Long> userIds) {
        var chat = new Chat(chatId, new Users(userIds));
        chatRepository.save(chat);
    }

    public void delete(Long chatId) {
        chatRepository.deleteById(chatId);
    }

    private List<Long> fetchChatParticipants(Long chatId) {
        var request = ParticipantIdsRequest.newBuilder()
                .setChatId(chatId)
                .build();
        return grpcClients.getChatStub().getChatParticipantIds(request).getParticipantsList();
    }
}
