package org.example.application.message;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PastOrPresent;
import lombok.RequiredArgsConstructor;
import org.example.application.dto.MessageDTO;
import org.example.application.dto.MessageEditRequest;
import org.example.application.dto.MessageRequest;
import org.example.application.message.command.CreateMessageCommand;
import org.example.application.message.command.DeleteMessageCommand;
import org.example.application.message.command.EditMessageCommand;
import org.example.application.message.command.GetMessagesCommand;
import org.example.application.message.command.MessageCommandHandler;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.example.common.Constants.USER_ID_HEADER;
import static org.springframework.http.HttpStatus.CREATED;

@CrossOrigin
@RestController
@RequiredArgsConstructor
@Validated
public class MessageController {
    private final MessageCommandHandler messageCommandHandler;

    @PostMapping(value = "/messages", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UUID> createMessage(
            @RequestHeader(USER_ID_HEADER) Long senderId,
            @RequestBody @Valid MessageRequest messageRequest
    ) {
        var command = new CreateMessageCommand(
                senderId, messageRequest.chatId(), messageRequest.content(), messageRequest.clientMessageId());
        var messageId = messageCommandHandler.handle(command);
        return ResponseEntity.status(CREATED).body(messageId);
    }

    @GetMapping("/messages")
    public ResponseEntity<List<MessageDTO>> getMessages(
            @RequestHeader(USER_ID_HEADER) Long senderId,
            @RequestParam Long chatId,
            @RequestParam(required = false) @PastOrPresent Instant before,
            @RequestParam(required = false, defaultValue = "${message.query.default.limit}") @Min(1) @Max(100) Integer limit
    ) {
        before = before == null ? Instant.now() : before;
        var command = new GetMessagesCommand(senderId, chatId, before, limit);
        return ResponseEntity.ok(messageCommandHandler.handle(command));
    }

    @PatchMapping("/messages")
    public ResponseEntity<Void> editMessage(
            @RequestHeader(USER_ID_HEADER) Long senderId,
            @RequestBody @Valid MessageEditRequest messageEditRequest
    ) {
        var command = new EditMessageCommand(
                senderId, messageEditRequest.chatId(), messageEditRequest.messageId(), messageEditRequest.content());
        messageCommandHandler.handle(command);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/messages")
    public ResponseEntity<Void> deleteMessage(
            @RequestHeader(USER_ID_HEADER) Long senderId,
            @RequestParam Long chatId,
            @RequestParam UUID messageId
    ) {
        var command = new DeleteMessageCommand(senderId, chatId, messageId);
        messageCommandHandler.handle(command);
        return ResponseEntity.ok().build();
    }
}
