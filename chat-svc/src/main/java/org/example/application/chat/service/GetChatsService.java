package org.example.application.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.ApplicationException;
import org.example.application.chat.dto.ChatTypeDTO;
import org.example.common.ChatApplicationError;
import org.example.domain.chat.ChatFacade;
import org.example.domain.chat.entity.ChatType;
import org.example.domain.chat.projection.ChatDetail;
import org.example.domain.message.MessageFacade;
import org.example.domain.user.UserDTO;
import org.example.domain.user.UserFacade;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class GetChatsService {
    private final ChatFacade chatFacade;
    private final UserFacade userFacade;
    private final MessageFacade messageFacade;
    private final ExecutorService asyncExecutor;

    public List<ChatDetail> getChats(Long userId, ChatTypeDTO chatType, Integer pageNumber, Integer pageSize) {
        var chatDetails = switch (chatType) {
            case PRIVATE -> chatFacade.findUserPrivateChatDetails(userId, pageNumber, pageSize);
            case GROUP -> chatFacade.findUserGroupChatDetails(userId, pageNumber, pageSize);
            case null -> chatFacade.findUserChatDetails(userId, pageNumber, pageSize);
        };

        var userIds = chatType == ChatTypeDTO.GROUP ? Set.<Long>of() : extractUserIds(chatDetails);
        if (userIds.isEmpty()) {
            populateLatestMessage(chatDetails);
            return chatDetails;
        }
        var usersFuture = CompletableFuture.supplyAsync(() -> userFacade.getUsers(userIds), asyncExecutor)
                .orTimeout(5, TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    throw new ApplicationException(ChatApplicationError.FAILED_TO_FETCH_USERS, ex);
                });

        populateLatestMessage(chatDetails);
        populatePrivateChatDetails(chatDetails, usersFuture.join());
        return chatDetails;
    }

    private void populatePrivateChatDetails(List<ChatDetail> chatDetails, Collection<UserDTO> users) {
        var usersMap = users.stream().collect(Collectors.toMap(UserDTO::id, Function.identity()));
        chatDetails.stream()
                .filter(chatDetail -> chatDetail.getChatType() == ChatType.PRIVATE)
                .forEach(chatDetail -> {
                    var userDTO = usersMap.get(chatDetail.getOtherUserId());
                    chatDetail.setName(userDTO.userName());
                    chatDetail.setImageUrl(userDTO.imageUrl());
                });
    }

    private void populateLatestMessage(List<ChatDetail> chatDetails) {
        var chatDetailMap = chatDetails.stream()
                .collect(Collectors.toMap(ChatDetail::getChatId, Function.identity()));
        var latestChatMessages = messageFacade.getLatestChatMessages(chatDetailMap.keySet());
        latestChatMessages.forEach(latestMessage -> {
            var chatDetail = chatDetailMap.get(latestMessage.chatId());
            if (!latestMessage.messages().isEmpty()) {
                chatDetail.setLatestMessage(latestMessage.messages().getFirst());
            }
        });
    }

    private static Set<Long> extractUserIds(List<ChatDetail> chatDetails) {
        return chatDetails.stream()
                .filter(chatDetail -> chatDetail.getChatType() == ChatType.PRIVATE)
                .map(ChatDetail::getOtherUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
