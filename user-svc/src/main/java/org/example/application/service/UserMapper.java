package org.example.application.service;

import org.example.application.dto.SignUpRequest;
import org.example.application.dto.UserDTO;
import org.example.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collection;
import java.util.List;

@Mapper
public interface UserMapper {

    @Mapping(target = "password", source = "password")
    User toUser(SignUpRequest signUpRequest, String password);

    UserDTO toUserDTO(User user);

    default List<UserDTO> toUserDTOs(Collection<User> users) {
        return users.stream()
                .map(this::toUserDTO)
                .toList();
    }
}
