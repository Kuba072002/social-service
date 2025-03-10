package org.example.application;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.application.dto.SignInRequest;
import org.example.application.dto.SignUpRequest;
import org.example.domain.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<Void> register(@Valid SignUpRequest signUpRequest) {
        userService.createUser(signUpRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/signIn")
    public ResponseEntity<Void> login(@Valid SignInRequest signInRequest) {
        userService.authUser(signInRequest.email(),signInRequest.password());
        return ResponseEntity.ok().build();
    }
}
