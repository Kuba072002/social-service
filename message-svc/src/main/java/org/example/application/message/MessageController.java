package org.example.application.message;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import lombok.RequiredArgsConstructor;
import org.example.dto.message.MessageDTO;
import org.example.dto.message.MessageEditRequest;
import org.example.dto.message.MessageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.CREATED;

@CrossOrigin
@RestController
@RequiredArgsConstructor
@Validated
public class MessageController {
    private final MessageService messageService;

    @PostMapping(value = "/messages", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UUID> createMessage(
            @RequestHeader(name = "userId") Long senderId,
            @RequestBody @Valid MessageRequest messageRequest
    ) {
        return ResponseEntity.status(CREATED)
                .body(messageService.createMessage(senderId, messageRequest));
    }

    @GetMapping("/messages")
    public ResponseEntity<List<MessageDTO>> getMessages(
            @RequestHeader(name = "userId") Long senderId,
            @RequestParam Long chatId,
            @RequestParam(required = false) @Past OffsetDateTime from,
            @RequestParam(required = false) @PastOrPresent OffsetDateTime to,
            @RequestParam(required = false) @Max(100) Integer limit
    ) {
        return ResponseEntity.ok(messageService.getMessages(
                senderId,
                chatId,
                from == null ? null : from.toInstant(),
                to == null ? null : to.toInstant(),
                limit
        ));
    }

    @PutMapping("/messages")
    public ResponseEntity<Void> editMessage(
            @RequestHeader(name = "userId") Long senderId,
            @RequestBody @Valid MessageEditRequest messageEditRequest
    ) {
        messageService.editMessage(senderId, messageEditRequest);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/messages")
    public ResponseEntity<Void> deleteMessage(
            @RequestHeader(name = "userId") Long senderId,
            @RequestParam Long chatId,
            @RequestParam UUID messageId
    ) {
        messageService.deleteMessage(senderId, chatId, messageId);
        return ResponseEntity.ok().build();
    }
}
