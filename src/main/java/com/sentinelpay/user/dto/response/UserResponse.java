package com.sentinelpay.user.dto.response;

import com.sentinelpay.user.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        UserStatus status,
        Set<String> roles,
        LocalDateTime createdAt
) {
}