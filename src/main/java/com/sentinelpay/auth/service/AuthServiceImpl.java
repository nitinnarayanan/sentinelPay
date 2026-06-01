package com.sentinelpay.auth.service;

import com.sentinelpay.auth.dto.request.LoginRequest;
import com.sentinelpay.auth.dto.request.LogoutRequest;
import com.sentinelpay.auth.dto.request.RefreshTokenRequest;
import com.sentinelpay.auth.dto.response.AuthResponse;
import com.sentinelpay.common.exception.BadRequestException;
import com.sentinelpay.common.response.MessageResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Override
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        throw new BadRequestException("Login is not implemented yet. Complete Stage 3.2 and Stage 3.4 first.");
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request, HttpServletRequest httpRequest) {
        throw new BadRequestException("Refresh token is not implemented yet. Complete Stage 3.5 and Stage 3.6 first.");
    }

    @Override
    public MessageResponse logout(LogoutRequest request, HttpServletRequest httpRequest) {
        throw new BadRequestException("Logout is not implemented yet. Complete Stage 3.7 first.");
    }
}