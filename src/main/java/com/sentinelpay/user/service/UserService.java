package com.sentinelpay.user.service;

import com.sentinelpay.user.dto.request.RegisterUserRequest;
import com.sentinelpay.user.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface UserService {

    UserResponse registerUser(RegisterUserRequest request, HttpServletRequest httpRequest);
}