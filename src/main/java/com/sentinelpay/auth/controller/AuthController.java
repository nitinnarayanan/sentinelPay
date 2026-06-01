package com.sentinelpay.auth.controller;

import com.sentinelpay.auth.dto.request.LoginRequest;
import com.sentinelpay.auth.dto.request.LogoutRequest;
import com.sentinelpay.auth.dto.request.RefreshTokenRequest;
import com.sentinelpay.auth.dto.response.AuthResponse;
import com.sentinelpay.auth.service.AuthService;
import com.sentinelpay.common.response.MessageResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        return authService.login(request, httpRequest);
    }

    @PostMapping("/refresh")
    public AuthResponse refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest
    ) {
        return authService.refreshToken(request, httpRequest);
    }

    @PostMapping("/logout")
    public MessageResponse logout(
            @Valid @RequestBody LogoutRequest request,
            HttpServletRequest httpRequest
    ) {
        return authService.logout(request, httpRequest);
    }
}