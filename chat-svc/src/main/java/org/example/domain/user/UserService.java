package org.example.domain.user;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.Set;

@HttpExchange(accept = "application/json", contentType = "application/json")
public interface UserService {
    @PostExchange("/internal/users")
    Set<UserDTO> getUsers(@RequestBody Set<Long> userIds);
}
