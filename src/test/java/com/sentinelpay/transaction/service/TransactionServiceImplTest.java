package com.sentinelpay.transaction.service;

import com.sentinelpay.account.entity.Account;
import com.sentinelpay.account.enums.AccountStatus;
import com.sentinelpay.account.enums.AccountType;
import com.sentinelpay.account.repository.AccountRepository;
import com.sentinelpay.audit.service.AuditService;
import com.sentinelpay.common.exception.BadRequestException;
import com.sentinelpay.common.security.SentinelPayUserPrincipal;
import com.sentinelpay.common.util.RequestMetadataUtil;
import com.sentinelpay.transaction.dto.request.CreateTransactionRequest;
import com.sentinelpay.transaction.dto.response.TransactionResponse;
import com.sentinelpay.transaction.entity.Transaction;
import com.sentinelpay.transaction.enums.TransactionStatus;
import com.sentinelpay.transaction.enums.TransactionType;
import com.sentinelpay.transaction.mapper.TransactionMapper;
import com.sentinelpay.transaction.repository.TransactionRepository;
import com.sentinelpay.user.entity.AppUser;
import com.sentinelpay.user.enums.UserStatus;
import com.sentinelpay.user.repository.AppUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Spy
    private TransactionMapper transactionMapper;

    @Mock
    private AuditService auditService;

    @Mock
    private RequestMetadataUtil requestMetadataUtil;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    void shouldCreateTransactionSuccessfully() {
        AppUser user = buildUser(UUID.randomUUID(), "audituser1@example.com");
        SentinelPayUserPrincipal principal = buildPrincipal(user);

        Account source = buildAccount(user, new BigDecimal("1000.00"), "ACC-SOURCE");
        Account destination = buildAccount(user, BigDecimal.ZERO, "ACC-DEST");

        CreateTransactionRequest request = new CreateTransactionRequest(
                source.getId(),
                destination.getId(),
                TransactionType.TRANSFER,
                new BigDecimal("100.00"),
                "USD",
                "Test transfer",
                "txn-test-001"
        );

        when(transactionRepository.existsByIdempotencyKey("txn-test-001")).thenReturn(false);
        when(appUserRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(accountRepository.findWithUserById(source.getId())).thenReturn(Optional.of(source));
        when(accountRepository.findWithUserById(destination.getId())).thenReturn(Optional.of(destination));
        when(requestMetadataUtil.getCorrelationId(httpRequest)).thenReturn("test-correlation-001");
        when(requestMetadataUtil.getClientIp(httpRequest)).thenReturn("127.0.0.1");
        when(requestMetadataUtil.getUserAgent(httpRequest)).thenReturn("JUnit");

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            transaction.setId(UUID.randomUUID());
            return transaction;
        });

        TransactionResponse response = transactionService.createTransaction(request, principal, httpRequest);

        assertThat(response.id()).isNotNull();
        assertThat(response.status()).isEqualTo(TransactionStatus.PENDING);
        assertThat(response.amount()).isEqualByComparingTo("100.00");
        assertThat(response.currency()).isEqualTo("USD");
        assertThat(response.idempotencyKey()).isEqualTo("txn-test-001");

        verify(auditService).recordEvent(any());
    }

    @Test
    void shouldRejectDuplicateIdempotencyKey() {
        AppUser user = buildUser(UUID.randomUUID(), "audituser1@example.com");
        SentinelPayUserPrincipal principal = buildPrincipal(user);

        CreateTransactionRequest request = new CreateTransactionRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                TransactionType.TRANSFER,
                new BigDecimal("100.00"),
                "USD",
                "Duplicate test",
                "txn-test-001"
        );

        when(transactionRepository.existsByIdempotencyKey("txn-test-001")).thenReturn(true);

        assertThatThrownBy(() -> transactionService.createTransaction(request, principal, httpRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Idempotency key already exists");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldRejectInsufficientBalance() {
        AppUser user = buildUser(UUID.randomUUID(), "audituser1@example.com");
        SentinelPayUserPrincipal principal = buildPrincipal(user);

        Account source = buildAccount(user, new BigDecimal("50.00"), "ACC-SOURCE");
        Account destination = buildAccount(user, BigDecimal.ZERO, "ACC-DEST");

        CreateTransactionRequest request = new CreateTransactionRequest(
                source.getId(),
                destination.getId(),
                TransactionType.TRANSFER,
                new BigDecimal("100.00"),
                "USD",
                "Insufficient balance test",
                "txn-test-002"
        );

        when(transactionRepository.existsByIdempotencyKey("txn-test-002")).thenReturn(false);
        when(appUserRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(accountRepository.findWithUserById(source.getId())).thenReturn(Optional.of(source));
        when(accountRepository.findWithUserById(destination.getId())).thenReturn(Optional.of(destination));

        assertThatThrownBy(() -> transactionService.createTransaction(request, principal, httpRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient balance");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldRejectSameSourceAndDestinationAccount() {
        AppUser user = buildUser(UUID.randomUUID(), "audituser1@example.com");
        SentinelPayUserPrincipal principal = buildPrincipal(user);

        Account source = buildAccount(user, new BigDecimal("1000.00"), "ACC-SAME");

        CreateTransactionRequest request = new CreateTransactionRequest(
                source.getId(),
                source.getId(),
                TransactionType.TRANSFER,
                new BigDecimal("100.00"),
                "USD",
                "Same account test",
                "txn-test-003"
        );

        when(transactionRepository.existsByIdempotencyKey("txn-test-003")).thenReturn(false);
        when(appUserRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(accountRepository.findWithUserById(source.getId())).thenReturn(Optional.of(source));

        assertThatThrownBy(() -> transactionService.createTransaction(request, principal, httpRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Source and destination accounts must be different");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldDenyTransactionWhenSourceAccountNotOwnedByUser() {
        AppUser owner = buildUser(UUID.randomUUID(), "owner@example.com");
        AppUser attacker = buildUser(UUID.randomUUID(), "attacker@example.com");
        SentinelPayUserPrincipal principal = buildPrincipal(attacker);

        Account source = buildAccount(owner, new BigDecimal("1000.00"), "ACC-SOURCE");
        Account destination = buildAccount(owner, BigDecimal.ZERO, "ACC-DEST");

        CreateTransactionRequest request = new CreateTransactionRequest(
                source.getId(),
                destination.getId(),
                TransactionType.TRANSFER,
                new BigDecimal("100.00"),
                "USD",
                "Ownership test",
                "txn-test-004"
        );

        when(transactionRepository.existsByIdempotencyKey("txn-test-004")).thenReturn(false);
        when(appUserRepository.findById(attacker.getId())).thenReturn(Optional.of(attacker));
        when(accountRepository.findWithUserById(source.getId())).thenReturn(Optional.of(source));
        when(accountRepository.findWithUserById(destination.getId())).thenReturn(Optional.of(destination));

        assertThatThrownBy(() -> transactionService.createTransaction(request, principal, httpRequest))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("permission");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldCancelPendingTransactionSuccessfully() {
        AppUser user = buildUser(UUID.randomUUID(), "audituser1@example.com");
        SentinelPayUserPrincipal principal = buildPrincipal(user);

        Account source = buildAccount(user, new BigDecimal("1000.00"), "ACC-SOURCE");
        Account destination = buildAccount(user, BigDecimal.ZERO, "ACC-DEST");

        Transaction transaction = buildTransaction(user, source, destination, TransactionStatus.PENDING);

        when(transactionRepository.findWithDetailsById(transaction.getId())).thenReturn(Optional.of(transaction));
        when(requestMetadataUtil.getCorrelationId(httpRequest)).thenReturn("test-cancel-001");
        when(requestMetadataUtil.getClientIp(httpRequest)).thenReturn("127.0.0.1");
        when(requestMetadataUtil.getUserAgent(httpRequest)).thenReturn("JUnit");
        when(transactionRepository.save(transaction)).thenReturn(transaction);

        TransactionResponse response = transactionService.cancelTransaction(
                transaction.getId(),
                principal,
                httpRequest
        );

        assertThat(response.status()).isEqualTo(TransactionStatus.CANCELLED);
        verify(auditService).recordEvent(any());
    }

    @Test
    void shouldRejectCancellingAlreadyCancelledTransaction() {
        AppUser user = buildUser(UUID.randomUUID(), "audituser1@example.com");
        SentinelPayUserPrincipal principal = buildPrincipal(user);

        Account source = buildAccount(user, new BigDecimal("1000.00"), "ACC-SOURCE");
        Account destination = buildAccount(user, BigDecimal.ZERO, "ACC-DEST");

        Transaction transaction = buildTransaction(user, source, destination, TransactionStatus.CANCELLED);

        when(transactionRepository.findWithDetailsById(transaction.getId())).thenReturn(Optional.of(transaction));

        assertThatThrownBy(() -> transactionService.cancelTransaction(transaction.getId(), principal, httpRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Only PENDING transactions can be cancelled");

        verify(auditService, never()).recordEvent(any());
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

    private Account buildAccount(AppUser user, BigDecimal balance, String accountNumber) {
        Account account = new Account();
        account.setId(UUID.randomUUID());
        account.setUser(user);
        account.setAccountNumber(accountNumber);
        account.setAccountType(AccountType.CHECKING);
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(balance);
        account.setCurrency("USD");
        return account;
    }

    private Transaction buildTransaction(
            AppUser user,
            Account source,
            Account destination,
            TransactionStatus status
    ) {
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setSourceAccount(source);
        transaction.setDestinationAccount(destination);
        transaction.setCreatedByUser(user);
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setStatus(status);
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setCurrency("USD");
        transaction.setDescription("Test transaction");
        transaction.setIdempotencyKey("txn-test");
        return transaction;
    }
}