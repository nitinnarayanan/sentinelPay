package com.sentinelpay.user.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private AppUserRepository appUserRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private AuditService auditService;
    private RequestMetadataUtil requestMetadataUtil;
    private UserMapper userMapper;
    private HttpServletRequest httpServletRequest;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        appUserRepository = mock(AppUserRepository.class);
        roleRepository = mock(RoleRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        auditService = mock(AuditService.class);
        requestMetadataUtil = mock(RequestMetadataUtil.class);
        userMapper = new UserMapper();
        httpServletRequest = mock(HttpServletRequest.class);

        userService = new UserServiceImpl(
                appUserRepository,
                roleRepository,
                passwordEncoder,
                auditService,
                requestMetadataUtil,
                userMapper
        );
    }

    @Test
    @DisplayName("Should register user with normalized email, hashed password, customer role, and audit event")
    void shouldRegisterUserSuccessfully() {
        RegisterUserRequest request = new RegisterUserRequest(
                "NewUser@Example.com",
                "Test@123",
                "New",
                "User"
        );

        Role customerRole = new Role();
        customerRole.setName("CUSTOMER");
        customerRole.setDescription("Default customer role");

        when(appUserRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(roleRepository.findByName("CUSTOMER")).thenReturn(Optional.of(customerRole));

        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        when(requestMetadataUtil.getCorrelationId(httpServletRequest)).thenReturn("test-correlation-001");
        when(requestMetadataUtil.getClientIp(httpServletRequest)).thenReturn("127.0.0.1");
        when(requestMetadataUtil.getUserAgent(httpServletRequest)).thenReturn("JUnit");

        UserResponse response = userService.registerUser(request, httpServletRequest);

        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo("newuser@example.com");
        assertThat(response.firstName()).isEqualTo("New");
        assertThat(response.lastName()).isEqualTo("User");
        assertThat(response.status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(response.roles()).containsExactly("CUSTOMER");

        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(userCaptor.capture());

        AppUser savedUser = userCaptor.getValue();

        assertThat(savedUser.getEmail()).isEqualTo("newuser@example.com");
        assertThat(savedUser.getPasswordHash()).isNotEqualTo("Test@123");
        assertThat(passwordEncoder.matches("Test@123", savedUser.getPasswordHash())).isTrue();
        assertThat(savedUser.getRoles()).contains(customerRole);

        verify(auditService, times(1)).recordEvent(any());
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when email already exists")
    void shouldThrowDuplicateResourceExceptionWhenEmailExists() {
        RegisterUserRequest request = new RegisterUserRequest(
                "Existing@Example.com",
                "Test@123",
                "Existing",
                "User"
        );

        when(appUserRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(request, httpServletRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Email already exists: existing@example.com");

        verify(appUserRepository, never()).save(any());
        verify(auditService, never()).recordEvent(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when default CUSTOMER role is missing")
    void shouldThrowResourceNotFoundExceptionWhenDefaultRoleMissing() {
        RegisterUserRequest request = new RegisterUserRequest(
                "rolemissing@example.com",
                "Test@123",
                "Role",
                "Missing"
        );

        when(appUserRepository.existsByEmail("rolemissing@example.com")).thenReturn(false);
        when(roleRepository.findByName("CUSTOMER")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.registerUser(request, httpServletRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Default role not found: CUSTOMER");

        verify(appUserRepository, never()).save(any());
        verify(auditService, never()).recordEvent(any());
    }
}