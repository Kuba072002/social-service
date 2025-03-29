package org.example.application;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import org.example.domain.message.Message;
import org.example.domain.message.MessageFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
public class MessageController {
    private final MessageFacade messageFacade;

    @PostMapping("/message")
    public ResponseEntity<Void> createMessage(
            @RequestHeader(name = "userId") Long senderId,
            @RequestBody @Valid MessageDTO messageDTO
    ) {
        messageFacade.createMessage(senderId, messageDTO);
        return ResponseEntity.status(CREATED).build();
    }

    @GetMapping("/message")
    public ResponseEntity<List<Message>> getMessages(
            @RequestHeader(name = "userId") Long senderId,
            @RequestParam Long chatId,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) @Max(100) Integer limit
    ) {
        return ResponseEntity.ok(messageFacade.getMessages(senderId, chatId, from,to,limit));
    }
}
