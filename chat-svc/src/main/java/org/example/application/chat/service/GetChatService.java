package org.example.application.chat.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.example.ApplicationException;
import org.example.application.chat.dto.ParticipantDTO;
import org.example.application.chat.service.mapper.ChatResponseMapper;
import org.example.domain.chat.ChatFacade;
import org.example.domain.chat.entity.ChatParticipant;
import org.example.domain.chat.projection.ChatDetail;
import org.example.domain.user.UserFacade;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.example.common.ChatApplicationError.CHAT_NOT_EXISTS;
import static org.example.common.ChatApplicationError.USER_DOES_NOT_BELONG_TO_CHAT;

@Service
@RequiredArgsConstructor
public class GetChatService {
    private final ChatFacade chatFacade;
    private final UserFacade userFacade;
    private final ChatResponseMapper chatResponseMapper;

    public List<ChatDetail> getChats(Long userId, Boolean isPrivate, Integer pageNumber, Integer pageSize) {
        if (BooleanUtils.isTrue(isPrivate)) {
            var chatDetails = chatFacade.findUserPrivateChatDetails(userId, pageNumber, pageSize);
            var userIds = chatDetails.stream()
                    .map(ChatDetail::getOtherUser)
                    .collect(Collectors.toSet());
            var usersMap = userFacade.getUsersMap(userIds);
            chatDetails.forEach(chatDetail -> {
                var userDTO = usersMap.get(chatDetail.getOtherUser());
                chatDetail.setName(userDTO.userName());
                chatDetail.setImageUrl(userDTO.imageUrl());
            });
            return chatDetails;
        } else {
            return chatFacade.findUserGroupChatDetails(userId, pageNumber, pageSize);
        }
    }

    public ChatDetail getChat(Long userId, Long chatId) {
        var chat = chatFacade.findChatWithParticipants(chatId)
                .orElseThrow(() -> new ApplicationException(CHAT_NOT_EXISTS));
        var userIds = getParticipantIdsAndValidateUser(chat.getParticipants(), userId);
        var usersMap = userFacade.getUsersMap(userIds);
        var participantDTOs = chatResponseMapper.toParticipantDTOs(chat.getParticipants(), usersMap);
        ChatDetail chatDetail = new ChatDetail(
                chatId, chat.getName(), chat.getImageUrl(), chat.getIsPrivate(), chat.getLastMessageAt(), null);
        chatDetail.setParticipants(participantDTOs);

        chat.getParticipants().stream()
                .filter(participant -> participant.getUserId().equals(userId))
                .findFirst()
                .ifPresent(participant -> chatDetail.setLastReadAt(participant.getLastReadAt()));

        if (BooleanUtils.isTrue(chat.getIsPrivate())) {
            chat.getParticipants().stream()
                    .map(ChatParticipant::getUserId)
                    .filter(id -> !id.equals(userId))
                    .findFirst()
                    .ifPresent(otherParticipantId -> {
                        chatDetail.setOtherUser(otherParticipantId);
                        var otherUser = usersMap.get(otherParticipantId);
                        chatDetail.setName(otherUser.userName());
                        chatDetail.setImageUrl(otherUser.imageUrl());
                    });
        }
        return chatDetail;
    }

    public List<ParticipantDTO> getParticipants(Long userId, Long chatId) {
        var participants = chatFacade.findChatParticipants(chatId);
        if (participants.isEmpty()) {
            throw new ApplicationException(CHAT_NOT_EXISTS);
        }
        var userIds = getParticipantIdsAndValidateUser(participants, userId);
        var usersMap = userFacade.getUsersMap(userIds);
        return chatResponseMapper.toParticipantDTOs(participants, usersMap);
    }

    public List<Long> getChatParticipantsIds(Long chatId) {
        var participantIds = chatFacade.findChatParticipantIds(chatId);
        if (participantIds.isEmpty()) {
            throw new ApplicationException(CHAT_NOT_EXISTS);
        }
        return participantIds;
    }

    private Set<Long> getParticipantIdsAndValidateUser(List<ChatParticipant> participants, Long userId) {
        var userIds = participants.stream()
                .map(ChatParticipant::getUserId)
                .collect(Collectors.toSet());
        if (!userIds.contains(userId)) {
            throw new ApplicationException(USER_DOES_NOT_BELONG_TO_CHAT);
        }
        return userIds;
    }
}
