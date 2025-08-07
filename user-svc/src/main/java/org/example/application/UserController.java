package org.example.application;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.example.application.service.AuthService;
import org.example.application.service.CreateUserService;
import org.example.application.service.GetUserService;
import org.example.dto.user.SignInRequest;
import org.example.dto.user.SignInResponse;
import org.example.dto.user.SignUpRequest;
import org.example.dto.user.UserDTO;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@CrossOrigin
@Controller
@RequiredArgsConstructor
public class UserController {
    private final CreateUserService createUserService;
    private final AuthService authService;
    private final GetUserService getUserService;

    @MutationMapping("register")
    public Long register(@Argument SignUpRequest signUpRequest) {
        return createUserService.createUser(signUpRequest);
    }

    @MutationMapping("login")
    public SignInResponse login(@Argument SignInRequest signInRequest) {
        return authService.authUser(signInRequest.email(), signInRequest.password());
    }

    @QueryMapping("users")
    public List<UserDTO> getUser(@Argument String userName) {
        return getUserService.get(userName);
    }

    @GetMapping(value = "/internal/users/{userId}")
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
