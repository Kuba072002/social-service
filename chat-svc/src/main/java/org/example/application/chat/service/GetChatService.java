package org.example.application.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.ApplicationException;
import org.example.application.chat.dto.ParticipantDTO;
import org.example.application.chat.service.mapper.ChatResponseMapper;
import org.example.domain.chat.ChatFacade;
import org.example.domain.chat.entity.ChatDetail;
import org.example.domain.chat.entity.ChatParticipant;
import org.example.domain.user.UserFacade;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${default.chat.page.size}")
    private Integer defaultPageSize;

    public List<ChatDetail> getChats(Long userId, boolean isPrivate, Integer pageNumber, Integer pageSize) {
        if (pageNumber == null) pageNumber = 1;
        if (pageSize == null) pageSize = defaultPageSize;
        if (!isPrivate) {
            return chatFacade.findUserGroupChatDetails(userId, pageNumber, pageSize);
        } else {
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
        }
    }

    public List<ParticipantDTO> getParticipants(Long userId, Long chatId) {
        var participants = getParticipants(chatId);
        var ids = participants.stream()
                .map(ChatParticipant::getUserId)
                .collect(Collectors.toSet());
        if (!ids.contains(userId)) {
            throw new ApplicationException(USER_DOES_NOT_BELONG_TO_CHAT);
        }
        var usersMap = userFacade.getUsersMap(ids);
        return participants.stream()
                .map(chatParticipant -> {
                    var userDTO = usersMap.get(chatParticipant.getUserId());
                    return chatResponseMapper.toParticipantDto(chatParticipant, userDTO);
                })
                .toList();
    }

    public Set<Long> getParticipantIds(Long chatId) {
        return getParticipants(chatId).stream()
                .map(ChatParticipant::getUserId)
                .collect(Collectors.toSet());
    }

    private List<ChatParticipant> getParticipants(Long chatId) {
        var participants = chatFacade.findChatParticipants(chatId);
        if (participants.isEmpty()) {
            throw new ApplicationException(CHAT_NOT_EXISTS);
        }
        return participants;
    }
}
