package com.example.TMDT_Backend.mapper;

import com.example.TMDT_Backend.dto.request.RegisterRequest;
import com.example.TMDT_Backend.dto.response.UserDTO;
import com.example.TMDT_Backend.entity.User;
import com.example.TMDT_Backend.entity.enums.AuthProvider;
import com.example.TMDT_Backend.entity.enums.Role;
import org.mapstruct.*;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "fullName", expression = "java(request.getFirstName() + \" \" + request.getLastName())")
    @Mapping(target = "role", constant = "USER")
    @Mapping(target = "provider", constant = "LOCAL")
    @Mapping(target = "enabled", constant = "false")
    @Mapping(target = "keyId", ignore = true) // hệ thống sinh keyId sau
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tokens", ignore = true)
    User toEntity(RegisterRequest request);

    UserDTO toDTO(User user);
}
