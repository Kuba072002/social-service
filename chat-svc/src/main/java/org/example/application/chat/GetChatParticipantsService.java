package org.example.application.chat;

import lombok.RequiredArgsConstructor;
import org.example.ApplicationException;
import org.example.application.chat.dto.ParticipantDTO;
import org.example.domain.chat.ChatFacade;
import org.example.domain.chat.entity.ChatParticipant;
import org.example.domain.user.UserFacade;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.example.common.ChatApplicationError.PARTICIPANTS_LIST_FOR_CHAT_IS_EMPTY;
import static org.example.common.ChatApplicationError.USER_DOES_NOT_BELONG_TO_CHAT;

@Service
@RequiredArgsConstructor
public class GetChatParticipantsService {
    private final ChatFacade chatFacade;
    private final UserFacade userFacade;
    private final ChatResponseMapper chatResponseMapper;

    public List<ParticipantDTO> get(Long userId, Long chatId) {
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

    public Set<Long> getIds(Long chatId) {
        return getParticipants(chatId).stream()
                .map(ChatParticipant::getUserId)
                .collect(Collectors.toSet());
    }

    private List<ChatParticipant> getParticipants(Long chatId) {
        var participants = chatFacade.findChatParticipants(chatId);
        if (participants.isEmpty()) {
            throw new ApplicationException(PARTICIPANTS_LIST_FOR_CHAT_IS_EMPTY);
        }
        return participants;
    }
}
