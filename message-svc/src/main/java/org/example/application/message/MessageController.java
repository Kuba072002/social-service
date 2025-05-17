package org.example.application.message;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import lombok.RequiredArgsConstructor;
import org.example.application.dto.MessageDTO;
import org.example.application.dto.MessageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@CrossOrigin
@RestController
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    @PostMapping(value = "/messages", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createMessage(
            @RequestHeader(name = "userId") Long senderId,
            @RequestBody @Valid MessageRequest messageRequest
    ) {
        messageService.createMessage(senderId, messageRequest);
        return ResponseEntity.status(CREATED).build();
    }

    @GetMapping("/messages")
    public ResponseEntity<List<MessageDTO>> getMessages(
            @RequestHeader(name = "userId") Long senderId,
            @RequestParam Long chatId,
            @RequestParam(required = false) @Valid @Past Instant from,
            @RequestParam(required = false) @Valid @PastOrPresent Instant to,
            @RequestParam(required = false) @Valid @Max(100) Integer limit
    ) {
        return ResponseEntity.ok(messageService.getMessages(senderId, chatId, from, to, limit));
    }
}
