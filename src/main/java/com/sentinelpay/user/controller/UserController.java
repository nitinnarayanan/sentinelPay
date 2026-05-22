package com.sentinelpay.user.controller;

import com.sentinelpay.user.dto.request.RegisterUserRequest;
import com.sentinelpay.user.dto.response.UserResponse;
import com.sentinelpay.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //This method receives request, validates DTO, calls service, returns response
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerUser(@Valid @RequestBody RegisterUserRequest request) {
        return userService.registerUser(request);
    }
}