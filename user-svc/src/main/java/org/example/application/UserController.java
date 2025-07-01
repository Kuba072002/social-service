package org.example.application;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.example.application.dto.SignInRequest;
import org.example.application.dto.SignInResponse;
import org.example.application.dto.SignUpRequest;
import org.example.application.dto.UserDTO;
import org.example.application.service.AuthService;
import org.example.application.service.CreateUserService;
import org.example.application.service.GetUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

import static org.springframework.http.HttpStatus.CREATED;

@CrossOrigin
@RestController
@RequiredArgsConstructor
public class UserController {
    private final CreateUserService createUserService;
    private final AuthService authService;
    private final GetUserService getUserService;

    @PostMapping("/register")
    public ResponseEntity<Long> register(@RequestBody @Valid SignUpRequest signUpRequest) {
        return ResponseEntity.status(CREATED)
                .body(createUserService.createUser(signUpRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<SignInResponse> login(@RequestBody @Valid SignInRequest signInRequest) {
        var response = authService.authUser(signInRequest.email(), signInRequest.password());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getUser(@RequestParam String userName) {
        var response = getUserService.get(userName);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/internal/users/{userId}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long userId) {
        var response = getUserService.get(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/internal/users")
    public ResponseEntity<Set<UserDTO>> findUsers(@RequestBody @NotEmpty Set<Long> userIds) {
        var response = getUserService.find(userIds);
        return ResponseEntity.ok(response);
    }
}
