package org.example.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.domain.chat.repository.ChatParticipantRepository;
import org.example.domain.chat.repository.ChatRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final PrivateChatRepository privateChatRepository;

    public boolean findPrivateChat(Long userId1, Long userId2){
        return privateChatRepository
                .existsBySenderIdAndRecipientIdOrRecipientIdAndSenderId(userId1,userId2);
    }

}
