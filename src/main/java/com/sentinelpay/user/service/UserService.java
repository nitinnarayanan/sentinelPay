package com.sentinelpay.user.service;

import com.sentinelpay.user.dto.request.RegisterUserRequest;
import com.sentinelpay.user.dto.response.UserResponse;
import jakarta.validation.Valid;

public interface UserService {

    UserResponse registerUser(RegisterUserRequest request);

}
