package org.example.domain.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class UserFacade {
    private final UserService userService;

    public void validateUsers(Set<Long> userIds){
        var users = userService.getUsers(userIds);
        if (users.size() != userIds.size()){
            throw new RuntimeException("Some users are invalid");
        }
    }
}
