package com.example.userapi.mapper;

import com.example.userapi.dto.response.RoleResponseDTO;
import com.example.userapi.model.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    RoleResponseDTO toDto(Role role);
}