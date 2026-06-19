package com.sentinelpay.auth.entity;

import com.sentinelpay.auth.enums.RefreshTokenStatus;
import com.sentinelpay.common.entity.BaseEntity;
import com.sentinelpay.user.entity.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RefreshTokenStatus status;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    public boolean isActive() {
        return status == RefreshTokenStatus.ACTIVE
                && revokedAt == null
                && expiresAt.isAfter(LocalDateTime.now());
    }

    public void revoke() {
        this.status = RefreshTokenStatus.REVOKED;
        this.revokedAt = LocalDateTime.now();
    }

    public void markExpired() {
        this.status = RefreshTokenStatus.EXPIRED;
    }
}