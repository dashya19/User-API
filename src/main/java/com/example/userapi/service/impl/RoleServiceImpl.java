package com.example.userapi.service.impl;

import com.example.userapi.exception.DuplicateRoleException;
import com.example.userapi.exception.RoleInUseException;
import com.example.userapi.model.Role;
import com.example.userapi.repository.RoleRepository;
import com.example.userapi.service.RoleService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    @Cacheable(value = "roles", key = "#roleName")
    public Role findOrCreateRole(String roleName) {
        return roleRepository.findByRoleName(roleName)
                .orElseGet(() -> {
                    if (roleRepository.existsByRoleName(roleName)) {
                        throw new DuplicateRoleException("Роль с именем '" + roleName + "' уже существует");
                    }
                    Role newRole = Role.builder()
                            .roleName(roleName)
                            .build();
                    return roleRepository.save(newRole);
                });
    }

    @Override
    @Cacheable(value = "roles", key = "#roleName")
    public Role findRoleByName(String roleName) {
        return roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new EntityNotFoundException("Роль с именем '" + roleName + "' не найдена"));
    }

    @Override
    public boolean existsByRoleName(String roleName) {
        return roleRepository.existsByRoleName(roleName);
    }

    @Override
    @Transactional
    @CacheEvict(value = "roles", key = "#roleId")
    public void deleteRoleIfNotInUse(UUID roleId) {
        if (roleRepository.isRoleInUse(roleId)) {
            throw new RoleInUseException("Невозможно удалить роль: она используется одним или несколькими пользователями");
        }
        roleRepository.deleteById(roleId);
    }

    @Override
    public boolean isRoleInUse(UUID roleId) {
        return roleRepository.isRoleInUse(roleId);
    }
}