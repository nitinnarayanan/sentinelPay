package com.sentinelpay.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SecurityTestController {

    @GetMapping("/api/v1/security/me")
    public Map<String, Object> me(Authentication authentication) {
        SentinelPayUserPrincipal principal =
                (SentinelPayUserPrincipal) authentication.getPrincipal();

        return Map.of(
                "userId", principal.getId(),
                "email", principal.getEmail(),
                "status", principal.getStatus(),
                "authorities", authentication.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList()
        );
    }
}