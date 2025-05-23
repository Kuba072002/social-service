package org.example.application.service;

import lombok.RequiredArgsConstructor;
import org.example.ApplicationException;
import org.example.application.dto.UserDTO;
import org.example.comon.UserApplicationError;
import org.example.domain.service.UserService;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class GetUserService {
    private final UserService userService;
    private final UserMapper userMapper;

    public UserDTO get(String userName) {
        var user = userService.findByUserName(userName)
                .orElseThrow(() -> new ApplicationException(UserApplicationError.USER_NOT_EXISTS));
        return userMapper.toUserDTO(user);
    }

    public Set<UserDTO> find(Set<Long> userIds) {
        return userMapper.toUserDTOs(userService.findByIds(userIds));
    }

}
