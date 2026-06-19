package com.sentinelpay.user.repository;

import com.sentinelpay.user.entity.AppUser;
import com.sentinelpay.user.enums.UserStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);

    List<AppUser> findByStatus(UserStatus status);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<AppUser> findWithRolesAndPermissionsByEmail(String email);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<AppUser> findWithRolesAndPermissionsById(UUID id);
}