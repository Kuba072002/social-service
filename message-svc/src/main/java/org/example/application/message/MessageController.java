package org.example.application.message;

import lombok.RequiredArgsConstructor;
import org.example.dto.message.MessageDTO;
import org.example.dto.message.MessageEditRequest;
import org.example.dto.message.MessageRequest;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@CrossOrigin
@Controller
@RequiredArgsConstructor
@Validated
public class MessageController {
    private final MessageService messageService;

    @MutationMapping("createMessage")
    public UUID createMessage(
            @Argument Long senderId,
            @Argument MessageRequest messageRequest
    ) {
        return messageService.createMessage(senderId, messageRequest);
    }

    @QueryMapping("getMessages")
    public List<MessageDTO> getMessages(
            @Argument Long senderId,
            @Argument Long chatId,
            @Argument OffsetDateTime from,
            @Argument OffsetDateTime to,
            @Argument Integer limit
    ) {
        return messageService.getMessages(
                senderId,
                chatId,
                from == null ? null : from.toInstant(),
                to == null ? null : to.toInstant(),
                limit
        );
    }

    @QueryMapping("editMessage")
    public Boolean editMessage(
            @Argument Long senderId,
            @Argument MessageEditRequest messageEditRequest
    ) {
        messageService.editMessage(senderId, messageEditRequest);
        return true;
    }

    @QueryMapping("deleteMessage")
    public Boolean deleteMessage(
            @Argument Long senderId,
            @Argument Long chatId,
            @Argument UUID messageId
    ) {
        messageService.deleteMessage(senderId, chatId, messageId);
        return true;
    }
}
