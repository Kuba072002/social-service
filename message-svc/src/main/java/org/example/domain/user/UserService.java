package org.example.domain.user;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(accept = "application/json", contentType = "application/json")
public interface UserService {
    @GetExchange("/internal/users/{userId}")
    UserDTO getUser(@PathVariable Long userId);
}
