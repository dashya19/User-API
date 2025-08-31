package com.example.userapi.mapper;

import com.example.userapi.dto.request.CreateUserRequestDTO;
import com.example.userapi.dto.request.UpdateUserRequestDTO;
import com.example.userapi.dto.response.UserResponseDTO;
import com.example.userapi.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = RoleMapper.class)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    User toEntity(CreateUserRequestDTO dto);

    UserResponseDTO toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    void updateEntityFromDto(UpdateUserRequestDTO dto, @MappingTarget User entity);
}