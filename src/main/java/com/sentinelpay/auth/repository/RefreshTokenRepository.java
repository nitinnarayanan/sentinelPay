package com.sentinelpay.auth.repository;

import com.sentinelpay.auth.entity.RefreshToken;
import com.sentinelpay.auth.enums.RefreshTokenStatus;
import com.sentinelpay.user.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findByUser(AppUser user);

    List<RefreshToken> findByUserAndStatus(AppUser user, RefreshTokenStatus status);

    List<RefreshToken> findByStatusAndExpiresAtBefore(
            RefreshTokenStatus status,
            LocalDateTime now
    );
}