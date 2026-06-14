package org.example.application.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.ApplicationException;
import org.example.application.chat.dto.ParticipantDTO;
import org.example.application.chat.service.mapper.ChatResponseMapper;
import org.example.domain.chat.ChatFacade;
import org.example.domain.chat.entity.ChatParticipant;
import org.example.domain.chat.entity.ChatType;
import org.example.domain.chat.projection.ChatDetail;
import org.example.domain.user.UserFacade;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.example.common.ChatApplicationError.CHAT_NOT_EXISTS;
import static org.example.common.ChatApplicationError.USER_DOES_NOT_BELONG_TO_CHAT;

@Service
@RequiredArgsConstructor
class GetChatService {
    private final ChatFacade chatFacade;
    private final UserFacade userFacade;
    private final ChatResponseMapper chatResponseMapper;

    public ChatDetail getChat(Long userId, Long chatId) {
        var chat = chatFacade.findChatWithParticipants(chatId)
                .orElseThrow(() -> new ApplicationException(CHAT_NOT_EXISTS));
        var participantsMap = getParticipantMapAndValidateUser(userId, chat.getParticipants());
        var senderParticipant = participantsMap.remove(userId);
        var usersMap = userFacade.getUsersMap(participantsMap.keySet());
        var participantDTOs = chatResponseMapper.toParticipantDTOs(participantsMap.values(), usersMap);
        ChatDetail chatDetail = chatResponseMapper.toChatDetail(chat, senderParticipant, participantDTOs);

        if (chat.getChatType() == ChatType.PRIVATE) {
            var otherParticipant = participantDTOs.getFirst();
            chatDetail.setOtherUserId(otherParticipant.userId());
            chatDetail.setName(otherParticipant.userName());
            chatDetail.setImageUrl(otherParticipant.imageUrl());
        }
        return chatDetail;
    }

    public List<ParticipantDTO> getParticipants(Long userId, Long chatId) {
        var participants = chatFacade.findChatParticipants(chatId);
        if (participants.isEmpty()) {
            throw new ApplicationException(CHAT_NOT_EXISTS);
        }
        var participantsMap = getParticipantMapAndValidateUser(userId, participants);
        participantsMap.remove(userId);
        var usersMap = userFacade.getUsersMap(participantsMap.keySet());
        return chatResponseMapper.toParticipantDTOs(participantsMap.values(), usersMap);
    }

    public List<Long> getChatParticipantsIds(Long chatId) {
        var participantIds = chatFacade.findChatParticipantIds(chatId);
        if (participantIds.isEmpty()) {
            throw new ApplicationException(CHAT_NOT_EXISTS);
        }
        return participantIds;
    }

    private static Map<Long, ChatParticipant> getParticipantMapAndValidateUser(
            Long userId, List<ChatParticipant> participants
    ) {
        var participantsMap = participants.stream()
                .collect(Collectors.toMap(ChatParticipant::getUserId, Function.identity()));
        if (!participantsMap.containsKey(userId)) {
            throw new ApplicationException(USER_DOES_NOT_BELONG_TO_CHAT);
        }
        return participantsMap;
    }
}
