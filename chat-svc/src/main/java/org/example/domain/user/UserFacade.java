package org.example.domain.user;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.example.ApplicationException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.example.common.ChatApplicationError.INVALID_USERS;

@Component
@RequiredArgsConstructor
public class UserFacade {
    private final UserService userService;

    public List<UserDTO> fetchAndValidateUsers(Set<Long> userIds) {
        var fetchedUsers = getUsers(userIds);
        validateUsers(userIds, fetchedUsers);
        return fetchedUsers;
    }

    public List<UserDTO> getUsers(Set<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        return userService.getUsers(userIds);
    }

    public Map<Long, UserDTO> getUsersMap(Set<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        return fetchAndValidateUsers(userIds).stream()
                .collect(Collectors.toMap(UserDTO::id, Function.identity()));
    }

    private static void validateUsers(Set<Long> userIds, List<UserDTO> fetchedUsers) {
        var fetchedUserIds = fetchedUsers.stream()
                .map(UserDTO::id)
                .collect(Collectors.toSet());
        var invalidUserIds = CollectionUtils.removeAll(userIds, fetchedUserIds);
        if (!invalidUserIds.isEmpty()) {
            throw new ApplicationException(INVALID_USERS.formatted(invalidUserIds));
        }
    }
}
