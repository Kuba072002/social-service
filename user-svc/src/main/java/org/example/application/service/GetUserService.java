package org.example.application.service;

import lombok.RequiredArgsConstructor;
import org.example.ApplicationException;
import org.example.application.dto.UserDTO;
import org.example.domain.service.UserService;
import org.springframework.stereotype.Service;

import java.util.Set;

import static org.example.comon.UserApplicationError.USER_NOT_EXISTS;

@Service
@RequiredArgsConstructor
public class GetUserService {
    private final UserService userService;
    private final UserMapper userMapper;

    public UserDTO get(String userName) {
        var user = userService.findByUserName(userName)
                .orElseThrow(() -> new ApplicationException(USER_NOT_EXISTS));
        return userMapper.toUserDTO(user);
    }

    public UserDTO get(Long userId) {
        var user = userService.findById(userId)
                .orElseThrow(() -> new ApplicationException(USER_NOT_EXISTS));
        return userMapper.toUserDTO(user);
    }

    public Set<UserDTO> find(Set<Long> userIds) {
        return userMapper.toUserDTOs(userService.findByIds(userIds));
    }

}
