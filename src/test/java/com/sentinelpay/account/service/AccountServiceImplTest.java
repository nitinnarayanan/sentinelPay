package com.sentinelpay.account.service;

import com.sentinelpay.account.dto.request.CreateAccountRequest;
import com.sentinelpay.account.dto.response.AccountResponse;
import com.sentinelpay.account.entity.Account;
import com.sentinelpay.account.enums.AccountStatus;
import com.sentinelpay.account.enums.AccountType;
import com.sentinelpay.account.mapper.AccountMapper;
import com.sentinelpay.account.repository.AccountRepository;
import com.sentinelpay.common.exception.ResourceNotFoundException;
import com.sentinelpay.common.security.SentinelPayUserPrincipal;
import com.sentinelpay.user.entity.AppUser;
import com.sentinelpay.user.enums.UserStatus;
import com.sentinelpay.user.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Spy
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    void shouldCreateAccountSuccessfully() {
        UUID userId = UUID.randomUUID();
        AppUser user = buildUser(userId, "audituser1@example.com");

        SentinelPayUserPrincipal principal = buildPrincipal(user);

        CreateAccountRequest request = new CreateAccountRequest(
                AccountType.CHECKING,
                "USD",
                new BigDecimal("1000.00")
        );

        when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            account.setId(UUID.randomUUID());
            return account;
        });

        AccountResponse response = accountService.createAccount(request, principal);

        assertThat(response.id()).isNotNull();
        assertThat(response.accountNumber()).startsWith("ACC-");
        assertThat(response.accountType()).isEqualTo(AccountType.CHECKING);
        assertThat(response.status()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(response.balance()).isEqualByComparingTo("1000.00");
        assertThat(response.currency()).isEqualTo("USD");

        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void shouldReturnMyAccounts() {
        UUID userId = UUID.randomUUID();
        AppUser user = buildUser(userId, "audituser1@example.com");
        SentinelPayUserPrincipal principal = buildPrincipal(user);

        Account account = buildAccount(user, new BigDecimal("1000.00"));

        when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(accountRepository.findByUser(user)).thenReturn(List.of(account));

        List<AccountResponse> responses = accountService.getMyAccounts(principal);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).id()).isEqualTo(account.getId());
        assertThat(responses.get(0).currency()).isEqualTo("USD");
    }

    @Test
    void shouldReturnMyAccountByIdWhenOwner() {
        UUID userId = UUID.randomUUID();
        AppUser user = buildUser(userId, "audituser1@example.com");
        SentinelPayUserPrincipal principal = buildPrincipal(user);

        Account account = buildAccount(user, new BigDecimal("1000.00"));

        when(accountRepository.findWithUserById(account.getId())).thenReturn(Optional.of(account));

        AccountResponse response = accountService.getMyAccountById(account.getId(), principal);

        assertThat(response.id()).isEqualTo(account.getId());
    }

    @Test
    void shouldDenyAccountAccessWhenNotOwner() {
        AppUser owner = buildUser(UUID.randomUUID(), "owner@example.com");
        AppUser anotherUser = buildUser(UUID.randomUUID(), "another@example.com");

        SentinelPayUserPrincipal principal = buildPrincipal(anotherUser);

        Account account = buildAccount(owner, new BigDecimal("1000.00"));

        when(accountRepository.findWithUserById(account.getId())).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.getMyAccountById(account.getId(), principal))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("permission");
    }

    @Test
    void shouldThrowResourceNotFoundWhenUserMissingDuringCreate() {
        UUID userId = UUID.randomUUID();
        AppUser user = buildUser(userId, "missing@example.com");
        SentinelPayUserPrincipal principal = buildPrincipal(user);

        CreateAccountRequest request = new CreateAccountRequest(
                AccountType.CHECKING,
                "USD",
                BigDecimal.ZERO
        );

        when(appUserRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.createAccount(request, principal))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    private AppUser buildUser(UUID id, String email) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setEmail(email);
        user.setPasswordHash("hashed-password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }

    private SentinelPayUserPrincipal buildPrincipal(AppUser user) {
        return new SentinelPayUserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getStatus(),
                Set.of(
                        new SimpleGrantedAuthority("ROLE_CUSTOMER"),
                        new SimpleGrantedAuthority("TRANSACTION_CREATE"),
                        new SimpleGrantedAuthority("TRANSACTION_VIEW_OWN")
                )
        );
    }

    private Account buildAccount(AppUser user, BigDecimal balance) {
        Account account = new Account();
        account.setId(UUID.randomUUID());
        account.setUser(user);
        account.setAccountNumber("ACC-20260620-123456");
        account.setAccountType(AccountType.CHECKING);
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(balance);
        account.setCurrency("USD");
        return account;
    }
}