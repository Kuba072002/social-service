package org.example.domain.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

@Component
@RequiredArgsConstructor
public class UserFacade {
    private final UserService userService;

    public void validateUser(Long userId) {
        if (isNull(userId)) return;
        userService.getUser(userId);
    }
}
