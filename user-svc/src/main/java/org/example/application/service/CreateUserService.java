package org.example.application.service;

import lombok.RequiredArgsConstructor;
import org.example.application.dto.SignUpRequest;
import org.example.application.exception.ApplicationException;
import org.example.comon.CustomErrorMessage;
import org.example.domain.entity.User;
import org.example.domain.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateUserService {
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final UserService userService;

    public void createUser(SignUpRequest signUpRequest) {
        validateIfPasswordMatch(signUpRequest.password(), signUpRequest.confirmPassword());
        validateIfUserExists(signUpRequest.userName(), signUpRequest.email());

        var encodedPassword = passwordEncoder.encode(signUpRequest.password());
        User user = userMapper.toUser(signUpRequest, encodedPassword);

        userService.save(user);
    }

    private void validateIfPasswordMatch(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new ApplicationException(CustomErrorMessage.CONFIRM_PASSWORD_DO_NOT_MATCH);
        }
    }

    private void validateIfUserExists(String userName, String email) {
        if (userService.validateIfExists(userName, email)) {
            throw new ApplicationException(CustomErrorMessage.USER_ALREADY_EXISTS);
        }
    }

}
