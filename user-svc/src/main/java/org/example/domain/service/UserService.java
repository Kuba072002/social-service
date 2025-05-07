package org.example.domain.service;

import lombok.RequiredArgsConstructor;
import org.example.domain.entity.User;
import org.example.domain.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

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

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByUserName(String userName) {
        return userRepository.findByUserName(userName);
    }

    public Collection<User> findByIds(Set<Long> userIds) {
        return userRepository.findAllById(userIds);
    }

}
