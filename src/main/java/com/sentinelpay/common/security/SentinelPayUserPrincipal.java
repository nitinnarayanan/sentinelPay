package com.sentinelpay.common.security;

import com.sentinelpay.user.entity.AppUser;
import com.sentinelpay.user.enums.UserStatus;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

@Getter
public class SentinelPayUserPrincipal implements UserDetails {

    private final UUID id;
    private final String email;
    private final String passwordHash;
    private final UserStatus status;
    private final Collection<? extends GrantedAuthority> authorities;

    public SentinelPayUserPrincipal(
            UUID id,
            String email,
            String passwordHash,
            UserStatus status,
            Collection<? extends GrantedAuthority> authorities
    ) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.status = status;
        this.authorities = authorities;
    }

    public static SentinelPayUserPrincipal from(
            AppUser user,
            Collection<? extends GrantedAuthority> authorities
    ) {
        return new SentinelPayUserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getStatus(),
                authorities
        );
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return status != UserStatus.INACTIVE;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.LOCKED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }
}