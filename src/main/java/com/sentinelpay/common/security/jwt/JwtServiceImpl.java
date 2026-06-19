package com.sentinelpay.common.security.jwt;

import com.sentinelpay.common.security.SentinelPayUserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

@Service
public class JwtServiceImpl implements JwtService {

    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;

    public JwtServiceImpl(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = Keys.hmacShaKeyFor(
                jwtProperties.secret().getBytes()
        );
    }

    @Override
    public String generateAccessToken(SentinelPayUserPrincipal principal) {
        Date now = new Date();
        Date expiration = getAccessTokenExpirationDate();

        Map<String, Object> claims = Map.of(
                "userId", principal.getId().toString(),
                "roles", principal.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .filter(authority -> authority.startsWith("ROLE_"))
                        .map(authority -> authority.replace("ROLE_", ""))
                        .toList(),
                "permissions", principal.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .filter(authority -> !authority.startsWith("ROLE_"))
                        .toList()
        );

        return Jwts.builder()
                .claims(claims)
                .subject(principal.getUsername())
                .issuer(jwtProperties.issuer())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    @Override
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    @Override
    public boolean isTokenValid(String token, SentinelPayUserPrincipal principal) {
        String username = extractUsername(token);
        return username.equals(principal.getUsername()) && !isTokenExpired(token);
    }

    @Override
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(jwtProperties.issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public Date getAccessTokenExpirationDate() {
        long expirationMillis = System.currentTimeMillis()
                + jwtProperties.accessTokenExpirationMinutes() * 60 * 1000;

        return new Date(expirationMillis);
    }

    @Override
    public LocalDateTime getAccessTokenExpirationDateTime() {
        return getAccessTokenExpirationDate()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token)
                .getExpiration()
                .before(new Date());
    }
}