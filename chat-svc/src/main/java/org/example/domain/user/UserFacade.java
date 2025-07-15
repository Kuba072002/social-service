package org.example.domain.user;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.example.ApplicationException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.example.common.ChatApplicationError.INVALID_USERS;

@Component
@RequiredArgsConstructor
public class UserFacade {
    private final UserService userService;

    public void validateUser(Long userId) {
        if (isNull(userId)) return;
        userService.getUser(userId);
    }

    public void validateUsers(Set<Long> userIds) {
        var fetchedUserIds = userService.getUsers(userIds).stream()
                .map(UserDTO::id)
                .collect(Collectors.toSet());
        var invalidUserIds = CollectionUtils.removeAll(userIds, fetchedUserIds);
        if (!invalidUserIds.isEmpty()) {
            throw new ApplicationException(INVALID_USERS.formatted(invalidUserIds));
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
