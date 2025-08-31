package com.example.userapi.repository;

import com.example.userapi.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByRoleName(String roleName);

    boolean existsByRoleName(String roleName);

    boolean existsByRoleNameAndIdNot(String roleName, UUID id);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.role.id = :roleId")
    boolean isRoleInUse(@Param("roleId") UUID roleId);
}
