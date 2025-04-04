package org.example.domain.service;

import lombok.RequiredArgsConstructor;
import org.example.ApplicationException;
import org.example.comon.UserApplicationError;
import org.example.domain.entity.User;
import org.example.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void save(User user) {
        userRepository.save(user);
    }

    public boolean validateIfExists(String userName, String email) {
        return userRepository.existsByUserNameOrEmail(userName, email);
    }

    public User getUser(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .orElseThrow(() -> new ApplicationException(UserApplicationError.INVALID_AUTH_DATA));
    }

    public User getUser(String userName) {
        return userRepository.findByUserName(userName)
                .orElseThrow(() -> new ApplicationException(UserApplicationError.USER_NOT_EXISTS));
    }

    public Collection<User> getUsers(Set<Long> userIds) {
        return userRepository.findAllById(userIds);
    }

}
