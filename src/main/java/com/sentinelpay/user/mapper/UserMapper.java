package com.sentinelpay.user.mapper;

import com.sentinelpay.auth.entity.Role;
import com.sentinelpay.user.dto.response.UserResponse;
import com.sentinelpay.user.entity.AppUser;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserResponse toUserResponse(AppUser user) {
        Set<String> roles = user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getStatus(),
                roles,
                user.getCreatedAt()
        );
    }
}