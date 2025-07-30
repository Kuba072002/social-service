package org.example.application.service;

import lombok.RequiredArgsConstructor;
import org.example.ApplicationException;
import org.example.domain.entity.User;
import org.example.domain.service.UserService;
import org.example.dto.user.SignUpRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static org.example.comon.UserApplicationError.CONFIRM_PASSWORD_DO_NOT_MATCH;
import static org.example.comon.UserApplicationError.USER_ALREADY_EXISTS;

@Service
@RequiredArgsConstructor
public class CreateUserService {
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final UserService userService;

    public Long createUser(SignUpRequest signUpRequest) {
        validateIfPasswordMatch(signUpRequest.password(), signUpRequest.confirmPassword());
        validateIfUserExists(signUpRequest.userName(), signUpRequest.email());

        var encodedPassword = passwordEncoder.encode(signUpRequest.password());
        User user = userMapper.toUser(signUpRequest, encodedPassword);

        userService.save(user);
        return user.getId();
    }

    private void validateIfPasswordMatch(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new ApplicationException(CONFIRM_PASSWORD_DO_NOT_MATCH);
        }
    }

    private void validateIfUserExists(String userName, String email) {
        if (userService.validateIfExists(userName, email)) {
            throw new ApplicationException(USER_ALREADY_EXISTS);
        }
    }

}
