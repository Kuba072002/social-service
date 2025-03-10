package org.example.application;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.application.dto.SignInRequest;
import org.example.application.dto.SignUpRequest;
import org.example.application.service.AuthService;
import org.example.application.service.CreateUserService;
import org.example.domain.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final CreateUserService createUserService;
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<Void> register(@Valid SignUpRequest signUpRequest) {
        createUserService.createUser(signUpRequest);
        return ResponseEntity.status(CREATED).build();
    }

    @PostMapping("/signIn")
    public ResponseEntity<User> login(@Valid SignInRequest signInRequest) {
        var user = authService.authUser(signInRequest.email(),signInRequest.password());
        return ResponseEntity.ok(user);
    }
}
