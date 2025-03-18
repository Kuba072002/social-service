package org.example.application;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.application.dto.SignInRequest;
import org.example.application.dto.SignUpRequest;
import org.example.application.dto.UserDTO;
import org.example.application.service.AuthService;
import org.example.application.service.CreateUserService;
import org.example.application.service.UserMapper;
import org.example.domain.entity.User;
import org.example.domain.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final CreateUserService createUserService;
    private final AuthService authService;
    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping("/signup")
    public ResponseEntity<Void> register(@Valid SignUpRequest signUpRequest) {
        createUserService.createUser(signUpRequest);
        return ResponseEntity.status(CREATED).build();
    }

    @PostMapping("/signIn")
    public ResponseEntity<User> login(@Valid SignInRequest signInRequest) {
        var user = authService.authUser(signInRequest.email(), signInRequest.password());
        return ResponseEntity.ok(user);
    }

    @GetMapping("/users")
    public ResponseEntity<UserDTO> getUser(String userName) {
        var response = userMapper.toUserDTO(userService.getUser(userName));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/internal/users")
    public ResponseEntity<Set<UserDTO>> getUsers(Set<Long> userIds) {
        var response = userMapper.toUserDTOs(userService.getUsers(userIds));
        return ResponseEntity.ok(response);
    }
}
