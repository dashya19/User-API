package com.example.userapi.service.impl;

import com.example.userapi.dto.request.CreateUserRequestDTO;
import com.example.userapi.dto.request.UpdateUserRequestDTO;
import com.example.userapi.dto.response.UserResponseDTO;
import com.example.userapi.exception.DuplicatePhoneNumberException;
import com.example.userapi.mapper.UserMapper;
import com.example.userapi.model.User;
import com.example.userapi.repository.UserRepository;
import com.example.userapi.service.RoleService;
import com.example.userapi.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final UserMapper userMapper;

    @Override
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public UserResponseDTO createUser(CreateUserRequestDTO request) {
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicatePhoneNumberException("Пользователь с номером телефона '" + request.getPhoneNumber() + "' уже существует");
        }

        User user = userMapper.toEntity(request);
        user.setRole(roleService.findOrCreateRole(request.getRoleName()));

        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    @Cacheable(value = "users", key = "#id")
    public UserResponseDTO getUserById(UUID id) {
        User user = userRepository.findByIdWithRole(id)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с ID '" + id + "' не найден"));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"users", "roles"}, allEntries = true)
    public UserResponseDTO updateUser(UpdateUserRequestDTO request) {
        User user = userRepository.findByIdWithRole(request.getId())
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с ID '" + request.getId() + "' не найден"));

        if (request.getPhoneNumber() != null &&
                !request.getPhoneNumber().equals(user.getPhoneNumber()) &&
                userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicatePhoneNumberException("Пользователь с номером телефона '" + request.getPhoneNumber() + "' уже существует");
        }

        userMapper.updateEntityFromDto(request, user);

        if (request.getRoleName() != null && !request.getRoleName().equals(user.getRole().getRoleName())) {
            user.setRole(roleService.findOrCreateRole(request.getRoleName()));
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toDto(updatedUser);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"users", "roles"}, allEntries = true)
    public void deleteUser(UUID id) {
        User user = userRepository.findByIdWithRole(id)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с ID '" + id + "' не найден"));

        userRepository.delete(user);

        UUID roleId = user.getRole().getId();
        if (!roleService.isRoleInUse(roleId)) {
            roleService.deleteRoleIfNotInUse(roleId);
        }
    }

    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }
}