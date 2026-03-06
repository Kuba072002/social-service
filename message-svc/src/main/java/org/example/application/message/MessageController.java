package org.example.application.message;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import lombok.RequiredArgsConstructor;
import org.example.application.dto.MessageDTO;
import org.example.application.dto.MessageEditRequest;
import org.example.application.dto.MessageRequest;
import org.example.application.message.command.CreateMessageCommand;
import org.example.application.message.command.DeleteMessageCommand;
import org.example.application.message.command.EditMessageCommand;
import org.example.application.message.command.MessageCommandHandler;
import org.example.application.message.service.MessageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    private final MessageService messageService;

    @PostMapping(value = "/messages", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UUID> createMessage(
            @RequestHeader(USER_ID_HEADER) Long senderId,
            @RequestBody @Valid MessageRequest messageRequest
    ) {
        var command = new CreateMessageCommand(senderId, messageRequest.chatId(), messageRequest.content());
        var messageId = messageCommandHandler.handle(command);
        return ResponseEntity.status(CREATED).body(messageId);
    }

    @GetMapping("/messages")
    public ResponseEntity<List<MessageDTO>> getMessages(
            @RequestHeader(USER_ID_HEADER) Long senderId,
            @RequestParam Long chatId,
            @RequestParam(required = false) @Past Instant from,
            @RequestParam(required = false) @PastOrPresent Instant to,
            @RequestParam(required = false, defaultValue = "${message.query.default.limit}") @Max(100) Integer limit
    ) {
        return ResponseEntity.ok(messageService.getMessages(senderId, chatId, from, to, limit));
    }

    @PutMapping("/messages")
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
