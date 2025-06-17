package org.example.domain.user;

import lombok.RequiredArgsConstructor;
import org.example.ApplicationException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.example.common.ChatApplicationError.INVALID_USERS;

@Component
@RequiredArgsConstructor
public class UserFacade {
    private final UserService userService;

    public void validateUsers(Set<Long> userIds) {
        var users = userService.getUsers(userIds);
        if (users.size() != userIds.size()) {
            var invalidUserIds = users.stream()
                    .map(UserDTO::id)
                    .collect(Collectors.toSet());
            userIds.removeAll(invalidUserIds);
            throw new ApplicationException(INVALID_USERS.formatted(userIds));
        }
    }

    public Map<Long, UserDTO> getUsersMap(Collection<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userService.getUsers(userIds).stream()
                .collect(Collectors.toMap(UserDTO::id, Function.identity()));
    }
}
