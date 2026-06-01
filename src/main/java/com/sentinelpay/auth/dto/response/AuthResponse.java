package com.sentinelpay.auth.dto.response;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
//This response is designed for the final login/refresh flow.
public record AuthResponse(
        UUID userId,
        String email,
        Set<String> roles,
        Set<String> permissions,
        String accessToken,
        String refreshToken,
        String tokenType,
        LocalDateTime accessTokenExpiresAt,
        LocalDateTime refreshTokenExpiresAt
) {
}