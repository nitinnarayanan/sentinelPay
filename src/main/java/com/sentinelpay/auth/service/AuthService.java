package com.sentinelpay.auth.service;

import com.sentinelpay.auth.dto.request.LoginRequest;
import com.sentinelpay.auth.dto.request.LogoutRequest;
import com.sentinelpay.auth.dto.request.RefreshTokenRequest;
import com.sentinelpay.auth.dto.response.AuthResponse;
import com.sentinelpay.common.response.MessageResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    AuthResponse login(LoginRequest request, HttpServletRequest httpRequest);

    AuthResponse refreshToken(RefreshTokenRequest request, HttpServletRequest httpRequest);

    MessageResponse logout(LogoutRequest request, HttpServletRequest httpRequest);
}