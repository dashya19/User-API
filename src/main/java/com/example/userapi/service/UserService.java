package com.example.userapi.service;

import com.example.userapi.dto.request.CreateUserRequestDTO;
import com.example.userapi.dto.request.UpdateUserRequestDTO;
import com.example.userapi.dto.response.UserResponseDTO;

import java.util.UUID;

public interface UserService {
    UserResponseDTO createUser(CreateUserRequestDTO request);
    UserResponseDTO getUserById(UUID id);
    UserResponseDTO updateUser(UpdateUserRequestDTO request);
    void deleteUser(UUID id);
    boolean existsByPhoneNumber(String phoneNumber);
}
