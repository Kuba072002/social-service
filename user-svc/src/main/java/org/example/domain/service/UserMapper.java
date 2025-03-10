package org.example.domain.service;

import org.example.application.dto.SignUpRequest;
import org.example.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UserMapper {

    @Mapping(target = "password", source = "password")
    User toUser(SignUpRequest signUpRequest, String password);
}
