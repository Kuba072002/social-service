package org.example.application.service;

import lombok.RequiredArgsConstructor;
import org.example.domain.entity.User;
import org.example.domain.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;

    public User authUser(String email,String password){
        return userService.getUser(email,password);
    }
}
