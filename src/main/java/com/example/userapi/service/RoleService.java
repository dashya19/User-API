package com.example.userapi.service;

import com.example.userapi.model.Role;

import java.util.UUID;

public interface RoleService {
    Role findOrCreateRole(String roleName);
    Role findRoleByName(String roleName);
    boolean existsByRoleName(String roleName);
    void deleteRoleIfNotInUse(UUID roleId);
    boolean isRoleInUse(UUID roleId);
}