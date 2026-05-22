package com.sentinelpay.user.service;

import com.sentinelpay.audit.dto.AuditEventCommand;
import com.sentinelpay.audit.enums.AuditAction;
import com.sentinelpay.audit.enums.AuditResourceType;
import com.sentinelpay.audit.service.AuditService;
import com.sentinelpay.auth.entity.Role;
import com.sentinelpay.auth.repository.RoleRepository;
import com.sentinelpay.common.exception.DuplicateResourceException;
import com.sentinelpay.common.exception.ResourceNotFoundException;
import com.sentinelpay.common.util.RequestMetadataUtil;
import com.sentinelpay.user.dto.request.RegisterUserRequest;
import com.sentinelpay.user.dto.response.UserResponse;
import com.sentinelpay.user.entity.AppUser;
import com.sentinelpay.user.enums.UserStatus;
import com.sentinelpay.user.mapper.UserMapper;
import com.sentinelpay.user.repository.AppUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String DEFAULT_ROLE = "CUSTOMER";

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final RequestMetadataUtil requestMetadataUtil;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse registerUser(RegisterUserRequest request, HttpServletRequest httpRequest) {
        String normalizedEmail = normalizeEmail(request.email());

        validateEmailIsUnique(normalizedEmail);

        Role customerRole = getDefaultCustomerRole();

        AppUser user = buildNewUser(request, normalizedEmail, customerRole);

        AppUser savedUser = appUserRepository.save(user);

        recordUserRegisteredAuditEvent(savedUser, httpRequest);

        return userMapper.toUserResponse(savedUser);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private void validateEmailIsUnique(String email) {
        if (appUserRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already exists: " + email);
        }
    }

    private Role getDefaultCustomerRole() {
        return roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new ResourceNotFoundException("Default role not found: " + DEFAULT_ROLE));
    }

    private AppUser buildNewUser(
            RegisterUserRequest request,
            String normalizedEmail,
            Role customerRole
    ) {
        AppUser user = new AppUser();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setStatus(UserStatus.ACTIVE);
        user.getRoles().add(customerRole);
        return user;
    }

    private void recordUserRegisteredAuditEvent(AppUser savedUser, HttpServletRequest httpRequest) {
        AuditEventCommand command = new AuditEventCommand(
                savedUser.getId(),
                AuditAction.USER_REGISTERED,
                AuditResourceType.USER,
                savedUser.getId().toString(),
                "User registered with email: " + savedUser.getEmail(),
                requestMetadataUtil.getCorrelationId(httpRequest),
                requestMetadataUtil.getClientIp(httpRequest),
                requestMetadataUtil.getUserAgent(httpRequest)
        );

        auditService.recordEvent(command);
    }
}