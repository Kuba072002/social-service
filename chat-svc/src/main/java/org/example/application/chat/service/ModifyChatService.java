package org.example.application.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.ApplicationException;
import org.example.application.chat.service.mapper.ChatMapper;
import org.example.domain.chat.ChatFacade;
import org.example.domain.chat.entity.Chat;
import org.example.domain.chat.entity.ChatParticipant;
import org.example.domain.user.UserFacade;
import org.example.dto.chat.ModifyChatParticipantsRequest;
import org.example.dto.chat.ModifyChatRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.example.common.ChatApplicationError.*;
import static org.example.common.Constants.ADMIN_ROLE;

@Service
@RequiredArgsConstructor
public class ModifyChatService {
    private final ChatFacade chatFacade;
    private final UserFacade userFacade;
    private final ChatMapper chatMapper;

    public void modifyChatParticipants(Long userId, Long chatId, ModifyChatParticipantsRequest modifyRequest) {
        if (modifyRequest.userIdsToAdd().contains(userId) || modifyRequest.userIdsToDelete().contains(userId)) {
            throw new ApplicationException(REQUEST_CANNOT_CONTAIN_REQUESTER_ID);
        }
        var chat = chatFacade.findChatWithParticipants(chatId)
                .orElseThrow(() -> new ApplicationException(CHAT_NOT_EXISTS));
        validateIfIsNotPrivate(chat);
        validateIfUserIsAdmin(userId, chat);
        validateRequestedParticipants(modifyRequest, chat.getParticipants());
        var newChatParticipants = chatMapper.toChatParticipants(modifyRequest.userIdsToAdd(), chat);
        var participantsToDelete = chat.getParticipants().stream()
                .filter(participant -> modifyRequest.userIdsToDelete().contains(participant.getUserId()))
                .toList();
        chatFacade.modifyParticipants(chat, newChatParticipants, participantsToDelete);
    }

    public void updateLastReadAt(Long userId, Long chatId, Instant lastReadAt) {
        var chatParticipant = chatFacade.getChatParticipant(chatId, userId)
                .orElseThrow(() -> new ApplicationException(USER_DOES_NOT_BELONG_TO_CHAT));
        if (chatParticipant.getLastReadAt() == null || lastReadAt.isAfter(chatParticipant.getLastReadAt())) {
            chatParticipant.setLastReadAt(lastReadAt);
            chatFacade.save(chatParticipant);
        }
    }

    public void modifyChat(Long userId, Long chatId, ModifyChatRequest modifyChatRequest) {
        var chat = chatFacade.findChatWithParticipants(chatId)
                .orElseThrow(() -> new ApplicationException(CHAT_NOT_EXISTS));
        validateIfIsNotPrivate(chat);
        validateIfUserIsAdmin(userId, chat);
        chat.setName(modifyChatRequest.name());
        chat.setImageUrl(modifyChatRequest.imageUrl());
        chatFacade.save(chat);
    }

    private void validateRequestedParticipants(ModifyChatParticipantsRequest modifyRequest, List<ChatParticipant> chatParticipants) {
        var existedParticipantIds = chatParticipants.stream()
                .map(ChatParticipant::getUserId)
                .collect(Collectors.toSet());
        var anyParticipantAlreadyExists = modifyRequest.userIdsToAdd().stream()
                .anyMatch(existedParticipantIds::contains);
        if (anyParticipantAlreadyExists) {
            throw new ApplicationException(CHAT_PARTICIPANTS_ALREADY_EXISTS);
        }
        if (!existedParticipantIds.containsAll(modifyRequest.userIdsToDelete())) {
            throw new ApplicationException(CHAT_PARTICIPANTS_NOT_EXISTS);
        }
        userFacade.validateUsers(modifyRequest.userIdsToAdd());
    }

    private void validateIfUserIsAdmin(Long userId, Chat chat) {
        var userIsAdmin = chat.getParticipants().stream()
                .anyMatch(chatParticipant -> chatParticipant.getUserId().equals(userId)
                        && Objects.equals(chatParticipant.getRole(), ADMIN_ROLE));
        if (!userIsAdmin) {
            throw new ApplicationException(USER_IS_NOT_ADMIN);
        }
    }

    private void validateIfIsNotPrivate(Chat chat) {
        if (chat.getIsPrivate()) {
            throw new ApplicationException(CANNOT_MODIFY_PRIVATE_CHAT);
        }
    }
}
