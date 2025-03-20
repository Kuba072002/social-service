package org.example.domain.user;


import java.util.Set;

public interface UserService {
    Set<UserDTO> getUsers(Set<Long> userIds);
}
