package org.example.application.service;

import org.example.application.dto.SignUpRequest;
import org.example.application.dto.UserDTO;
import org.example.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper
public interface UserMapper {

    @Mapping(target = "password", source = "password")
    User toUser(SignUpRequest signUpRequest, String password);

    UserDTO toUserDTO(User user);

    default Set<UserDTO> toUserDTOs(Collection<User> users) {
        return users.stream()
                .map(this::toUserDTO)
                .collect(Collectors.toSet());
    }
}
