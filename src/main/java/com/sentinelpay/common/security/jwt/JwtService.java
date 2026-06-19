package com.sentinelpay.common.security.jwt;

import com.sentinelpay.common.security.SentinelPayUserPrincipal;
import io.jsonwebtoken.Claims;

import java.time.LocalDateTime;
import java.util.Date;

public interface JwtService {

    String generateAccessToken(SentinelPayUserPrincipal principal);

    String extractUsername(String token);

    boolean isTokenValid(String token, SentinelPayUserPrincipal principal);

    Claims extractAllClaims(String token);

    Date getAccessTokenExpirationDate();

    LocalDateTime getAccessTokenExpirationDateTime();
}