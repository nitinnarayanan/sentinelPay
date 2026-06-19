package com.sentinelpay.auth.service;

import com.sentinelpay.audit.dto.AuditEventCommand;
import com.sentinelpay.audit.enums.AuditAction;
import com.sentinelpay.audit.enums.AuditResourceType;
import com.sentinelpay.audit.service.AuditService;
import com.sentinelpay.auth.dto.request.LoginRequest;
import com.sentinelpay.auth.dto.request.LogoutRequest;
import com.sentinelpay.auth.dto.request.RefreshTokenRequest;
import com.sentinelpay.auth.dto.response.AuthResponse;
import com.sentinelpay.common.exception.BadRequestException;
import com.sentinelpay.common.response.MessageResponse;
import com.sentinelpay.common.security.SentinelPayUserPrincipal;
import com.sentinelpay.common.security.jwt.JwtService;
import com.sentinelpay.common.util.RequestMetadataUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuditService auditService;
    private final RequestMetadataUtil requestMetadataUtil;

    @Override
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String normalizedEmail = request.email().trim().toLowerCase();

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            normalizedEmail,
                            request.password()
                    )
            );

            SentinelPayUserPrincipal principal =
                    (SentinelPayUserPrincipal) authentication.getPrincipal();

            String accessToken = jwtService.generateAccessToken(principal);

            recordLoginAuditEvent(
                    principal.getId(),
                    AuditAction.USER_LOGIN_SUCCESS,
                    "User login successful: " + normalizedEmail,
                    httpRequest
            );

            return new AuthResponse(
                    principal.getId(),
                    principal.getEmail(),
                    extractRoles(principal),
                    extractPermissions(principal),
                    accessToken,
                    null,
                    "Bearer",
                    jwtService.getAccessTokenExpirationDateTime(),
                    null
            );

        } catch (BadCredentialsException exception) {
            recordLoginAuditEvent(
                    null,
                    AuditAction.USER_LOGIN_FAILED,
                    "Login failed due to bad credentials for email: " + normalizedEmail,
                    httpRequest
            );
            throw new BadRequestException("Invalid email or password");

        } catch (DisabledException exception) {
            recordLoginAuditEvent(
                    null,
                    AuditAction.USER_LOGIN_FAILED,
                    "Login failed because account is disabled: " + normalizedEmail,
                    httpRequest
            );
            throw new BadRequestException("User account is disabled");

        } catch (LockedException exception) {
            recordLoginAuditEvent(
                    null,
                    AuditAction.USER_LOGIN_FAILED,
                    "Login failed because account is locked: " + normalizedEmail,
                    httpRequest
            );
            throw new BadRequestException("User account is locked");
        }
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request, HttpServletRequest httpRequest) {
        throw new BadRequestException("Refresh token is not implemented yet. Complete Stage 3.5 and Stage 3.6 first.");
    }

    @Override
    public MessageResponse logout(LogoutRequest request, HttpServletRequest httpRequest) {
        throw new BadRequestException("Logout is not implemented yet. Complete Stage 3.7 first.");
    }

    private Set<String> extractRoles(SentinelPayUserPrincipal principal) {
        return principal.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.replace("ROLE_", ""))
                .collect(Collectors.toSet());
    }

    private Set<String> extractPermissions(SentinelPayUserPrincipal principal) {
        return principal.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> !authority.startsWith("ROLE_"))
                .collect(Collectors.toSet());
    }

    private void recordLoginAuditEvent(
            UUID actorUserId,
            AuditAction action,
            String details,
            HttpServletRequest httpRequest
    ) {
        auditService.recordEvent(new AuditEventCommand(
                actorUserId,
                action,
                AuditResourceType.USER,
                actorUserId != null ? actorUserId.toString() : null,
                details,
                requestMetadataUtil.getCorrelationId(httpRequest),
                requestMetadataUtil.getClientIp(httpRequest),
                requestMetadataUtil.getUserAgent(httpRequest)
        ));
    }
}