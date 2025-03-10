package org.example.domain.service;

import lombok.RequiredArgsConstructor;
import org.example.application.exception.ApplicationException;
import org.example.comon.CustomErrorMessage;
import org.example.domain.entity.User;
import org.example.domain.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public void save(User user) {
        userRepository.save(user);
    }

    public boolean validateIfExists(String userName, String email) {
        return userRepository.existsByUserNameOrEmail(userName, email);
    }

    public User getUser(String email, String encodedPassword) {
        return userRepository.findByEmail(email)
                .filter(user -> user.getPassword().equals(encodedPassword))
                .orElseThrow(() -> new ApplicationException(CustomErrorMessage.INVALID_AUTH_DATA));
    }

}
