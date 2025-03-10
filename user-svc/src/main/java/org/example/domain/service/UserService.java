package org.example.domain.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.application.dto.SignUpRequest;
import org.example.application.exception.ApplicationException;
import org.example.comon.CustomErrorMessage;
import org.example.domain.entity.User;
import org.example.domain.entity.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public void createUser(@Valid SignUpRequest signUpRequest) {
        validatePassword(signUpRequest);
        var encodedPassword = passwordEncoder.encode(signUpRequest.password());
        User user = userMapper.toUser(signUpRequest, encodedPassword);
        if (userRepository.existsByUserNameOrEmail(user.getUserName(), user.getEmail())) {
            throw new ApplicationException(CustomErrorMessage.USER_ALREADY_EXISTS);
        }
        userRepository.save(user);
    }

    public void authUser(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user == null || !password.equals(user.getPassword())) {
            throw new ApplicationException(CustomErrorMessage.INVALID_AUTH_DATA);
        }
    }

    private static void validatePassword(SignUpRequest signUpRequest) {
        if (!signUpRequest.password().equals(signUpRequest.confirmPassword())) {
            throw new ApplicationException(CustomErrorMessage.CONFIRM_PASSWORD_DO_NOT_MATCH);
        }
    }

}
