package com.sentinelpay.auth.service;

import com.sentinelpay.auth.entity.RefreshToken;
import com.sentinelpay.auth.enums.RefreshTokenStatus;
import com.sentinelpay.auth.repository.RefreshTokenRepository;
import com.sentinelpay.common.exception.BadRequestException;
import com.sentinelpay.common.security.jwt.JwtProperties;
import com.sentinelpay.user.entity.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    @Override
    @Transactional
    public String createRefreshToken(AppUser user) {
        String rawToken = generateRawToken();
        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setStatus(RefreshTokenStatus.ACTIVE);
        refreshToken.setExpiresAt(getRefreshTokenExpirationDateTime());

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    @Override
    public String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available", exception);
        }
    }

    @Override
    public LocalDateTime getRefreshTokenExpirationDateTime() {
        return LocalDateTime.now().plusDays(jwtProperties.refreshTokenExpirationDays());
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String rawToken) {
        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        if (!refreshToken.isActive()) {
            throw new BadRequestException("Refresh token is expired or revoked");
        }

        return refreshToken;
    }

    private String generateRawToken() {
        return UUID.randomUUID() + "." + UUID.randomUUID();
    }
}