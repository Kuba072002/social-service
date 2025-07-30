package org.example.domain.user;

import org.example.dto.user.UserDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.Collection;
import java.util.Set;

@HttpExchange(accept = "application/json", contentType = "application/json")
public interface UserService {
    @GetExchange("/internal/users/{userId}")
    UserDTO getUser(@PathVariable Long userId);

    @PostExchange("/internal/users")
    Set<UserDTO> getUsers(@RequestBody Collection<Long> userIds);
}
