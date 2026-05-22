package com.sentinelpay.user.service;

import com.sentinelpay.auth.entity.Role;
import com.sentinelpay.auth.repository.RoleRepository;
import com.sentinelpay.common.exception.DuplicateResourceException;
import com.sentinelpay.common.exception.ResourceNotFoundException;
import com.sentinelpay.user.dto.request.RegisterUserRequest;
import com.sentinelpay.user.dto.response.UserResponse;
import com.sentinelpay.user.entity.AppUser;
import com.sentinelpay.user.enums.UserStatus;
import com.sentinelpay.user.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String DEFAULT_ROLE = "CUSTOMER";

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    //This method hashs password, queries repository directly, assigns roles, contains business rules
    @Override
    @Transactional
    public UserResponse registerUser(RegisterUserRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        if (appUserRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("Email already exists: " + normalizedEmail);
        }

        Role customerRole = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new ResourceNotFoundException("Default role not found: " + DEFAULT_ROLE));

        AppUser user = new AppUser();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setStatus(UserStatus.ACTIVE);
        user.getRoles().add(customerRole);

        AppUser savedUser = appUserRepository.save(user);

        return toUserResponse(savedUser);
    }

    private UserResponse toUserResponse(AppUser user) {
        Set<String> roles = user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getStatus(),
                roles,
                user.getCreatedAt()
        );
    }

}