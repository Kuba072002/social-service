package org.example.application;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Void> register(@RequestBody @Valid SignUpRequest signUpRequest) {
        createUserService.createUser(signUpRequest);
        return ResponseEntity.status(CREATED).build();
    }

    @PostMapping("/signIn")
    public ResponseEntity<User> login(@RequestBody @Valid SignInRequest signInRequest) {
        var user = authService.authUser(signInRequest.email(), signInRequest.password());
        return ResponseEntity.ok(user);
    }

    @GetMapping("/users")
    public ResponseEntity<UserDTO> getUser(@RequestParam String userName) {
        var response = userMapper.toUserDTO(userService.getUser(userName));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/internal/users")
    public ResponseEntity<Set<UserDTO>> getUsers(@RequestBody @NotEmpty Set<Long> userIds) {
        var response = userMapper.toUserDTOs(userService.getUsers(userIds));
        return ResponseEntity.ok(response);
    }
}
