package com.sentinelpay.auth.service;

import com.sentinelpay.auth.entity.RefreshToken;
import com.sentinelpay.user.entity.AppUser;

import java.time.LocalDateTime;

public interface RefreshTokenService {

    String createRefreshToken(AppUser user);

    String rotateRefreshToken(RefreshToken existingRefreshToken);

    String hashToken(String rawToken);

    LocalDateTime getRefreshTokenExpirationDateTime();

    RefreshToken validateRefreshToken(String rawToken);
}