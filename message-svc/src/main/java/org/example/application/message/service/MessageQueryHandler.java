package org.example.application.message.service;

import lombok.RequiredArgsConstructor;
import org.example.ApplicationException;
import org.example.application.chat.ChatAccessValidator;
import org.example.application.dto.LatestChatMessagesDTO;
import org.example.application.dto.MessageDTO;
import org.example.application.message.command.GetMessagesForChatsQuery;
import org.example.application.message.command.GetMessagesQuery;
import org.example.common.MessageApplicationError;
import org.example.domain.message.MessageFacade;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageQueryHandler {
    private final MessageFacade messageFacade;
    private final ChatAccessValidator chatAccessValidator;
    private final ExecutorService asyncExecutor;

    public List<MessageDTO> handle(GetMessagesQuery command) {
        chatAccessValidator.validateParticipant(command.chatId(), command.userId());
        return messageFacade.getMessages(command.chatId(), command.before(), command.limit());
    }

    public List<LatestChatMessagesDTO> handle(GetMessagesForChatsQuery command) {
        Instant now = Instant.now();
        var futureMessagesMap = command.chatIds().stream()
                .collect(Collectors.toMap(Function.identity(), chatId ->
                        CompletableFuture.supplyAsync(() -> messageFacade.getMessages(chatId, now, command.limit()), asyncExecutor)));

        CompletableFuture.allOf(futureMessagesMap.values().toArray(CompletableFuture[]::new))
                .orTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    throw new ApplicationException(MessageApplicationError.FAILED_TO_FETCH_MESSAGES, ex);
                }).join();

        return futureMessagesMap.entrySet()
                .stream()
                .map(entry -> new LatestChatMessagesDTO(entry.getKey(), entry.getValue().join()))
                .toList();
    }
}
