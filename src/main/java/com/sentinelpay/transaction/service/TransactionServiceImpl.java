package com.sentinelpay.transaction.service;

import com.sentinelpay.account.entity.Account;
import com.sentinelpay.account.repository.AccountRepository;
import com.sentinelpay.audit.dto.AuditEventCommand;
import com.sentinelpay.audit.enums.AuditAction;
import com.sentinelpay.audit.enums.AuditResourceType;
import com.sentinelpay.audit.service.AuditService;
import com.sentinelpay.common.exception.BadRequestException;
import com.sentinelpay.common.exception.ResourceNotFoundException;
import com.sentinelpay.common.security.SentinelPayUserPrincipal;
import com.sentinelpay.common.util.RequestMetadataUtil;
import com.sentinelpay.transaction.dto.request.CreateTransactionRequest;
import com.sentinelpay.transaction.dto.response.TransactionResponse;
import com.sentinelpay.transaction.entity.Transaction;
import com.sentinelpay.transaction.enums.TransactionStatus;
import com.sentinelpay.transaction.mapper.TransactionMapper;
import com.sentinelpay.transaction.repository.TransactionRepository;
import com.sentinelpay.user.entity.AppUser;
import com.sentinelpay.user.repository.AppUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.function.Consumer;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final AppUserRepository appUserRepository;
    private final TransactionMapper transactionMapper;
    private final AuditService auditService;
    private final RequestMetadataUtil requestMetadataUtil;

    @Override
    @Transactional
    public TransactionResponse createTransaction(
            CreateTransactionRequest request,
            SentinelPayUserPrincipal principal,
            HttpServletRequest httpRequest
    ) {
        validateIdempotencyKeyIsUnique(request.idempotencyKey());

        AppUser createdByUser = appUserRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + principal.getId()));

        Account sourceAccount = accountRepository.findWithUserById(request.sourceAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Source account not found with id: " + request.sourceAccountId()));

        Account destinationAccount = accountRepository.findWithUserById(request.destinationAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination account not found with id: " + request.destinationAccountId()));

        validateOwnership(sourceAccount, principal);
        validateAccountsForTransfer(sourceAccount, destinationAccount, request);

        Transaction transaction = new Transaction();
        transaction.setSourceAccount(sourceAccount);
        transaction.setDestinationAccount(destinationAccount);
        transaction.setTransactionType(request.transactionType());
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setAmount(request.amount());
        transaction.setCurrency(request.currency().toUpperCase());
        transaction.setDescription(request.description());
        transaction.setIdempotencyKey(request.idempotencyKey());
        transaction.setCreatedByUser(createdByUser);

        Transaction savedTransaction = transactionRepository.save(transaction);

        recordTransactionCreatedAuditEvent(savedTransaction, principal, httpRequest);

        return transactionMapper.toTransactionResponse(savedTransaction);
    }

    private void validateIdempotencyKeyIsUnique(String idempotencyKey) {
        if (transactionRepository.existsByIdempotencyKey(idempotencyKey)) {
            throw new BadRequestException("Duplicate transaction request. Idempotency key already exists.");
        }
    }

    private void validateOwnership(Account sourceAccount, SentinelPayUserPrincipal principal) {
        if (!sourceAccount.getUser().getId().equals(principal.getId())) {
            throw new AccessDeniedException("You do not have permission to use this source account");
        }
    }

    private void validateAccountsForTransfer(
            Account sourceAccount,
            Account destinationAccount,
            CreateTransactionRequest request
    ) {
        if (sourceAccount.getId().equals(destinationAccount.getId())) {
            throw new BadRequestException("Source and destination accounts must be different");
        }

        if (!sourceAccount.isActive()) {
            throw new BadRequestException("Source account is not active");
        }

        if (!destinationAccount.isActive()) {
            throw new BadRequestException("Destination account is not active");
        }

        String requestCurrency = request.currency().toUpperCase();

        if (!sourceAccount.getCurrency().equals(requestCurrency)) {
            throw new BadRequestException("Transaction currency must match source account currency");
        }

        if (!destinationAccount.getCurrency().equals(requestCurrency)) {
            throw new BadRequestException("Transaction currency must match destination account currency");
        }

        if (!sourceAccount.hasSufficientBalance(request.amount())) {
            throw new BadRequestException("Insufficient balance");
        }
    }

    private void recordTransactionCreatedAuditEvent(
            Transaction transaction,
            SentinelPayUserPrincipal principal,
            HttpServletRequest httpRequest
    ) {
        String details = "Transaction created with amount "
                + transaction.getAmount()
                + " "
                + transaction.getCurrency()
                + " from account "
                + transaction.getSourceAccount().getAccountNumber()
                + " to account "
                + transaction.getDestinationAccount().getAccountNumber();

        auditService.recordEvent(new AuditEventCommand(
                principal.getId(),
                AuditAction.TRANSACTION_CREATED,
                AuditResourceType.TRANSACTION,
                transaction.getId().toString(),
                details,
                requestMetadataUtil.getCorrelationId(httpRequest),
                requestMetadataUtil.getClientIp(httpRequest),
                requestMetadataUtil.getUserAgent(httpRequest)
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getMyTransactions(SentinelPayUserPrincipal principal) {
        AppUser user = appUserRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + principal.getId()));

        return transactionRepository
                .findByCreatedByUserOrSourceAccountUserOrDestinationAccountUser(user, user, user)
                .stream()
                .map(transactionMapper::toTransactionResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getMyTransactionById(
            UUID transactionId,
            SentinelPayUserPrincipal principal
    ) {
        Transaction transaction = transactionRepository.findWithDetailsById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + transactionId));

        validateTransactionVisibility(transaction, principal);

        return transactionMapper.toTransactionResponse(transaction);
    }

    private void validateTransactionVisibility(
            Transaction transaction,
            SentinelPayUserPrincipal principal
    ) {
        boolean createdByUser = transaction.getCreatedByUser()
                .getId()
                .equals(principal.getId());

        boolean sourceAccountOwner = transaction.getSourceAccount()
                .getUser()
                .getId()
                .equals(principal.getId());

        boolean destinationAccountOwner = transaction.getDestinationAccount()
                .getUser()
                .getId()
                .equals(principal.getId());

        if (!createdByUser && !sourceAccountOwner && !destinationAccountOwner) {
            throw new AccessDeniedException("You do not have permission to access this transaction");
        }
    }

    @Override
    @Transactional
    public TransactionResponse approveTransaction(
            UUID transactionId,
            SentinelPayUserPrincipal principal,
            HttpServletRequest httpRequest
    ) {
        return transitionTransactionStatus(
                transactionId,
                principal,
                httpRequest,
                Transaction::approve,
                AuditAction.TRANSACTION_APPROVED,
                "Transaction approved"
        );
    }

    @Override
    @Transactional
    public TransactionResponse blockTransaction(
            UUID transactionId,
            SentinelPayUserPrincipal principal,
            HttpServletRequest httpRequest
    ) {
        return transitionTransactionStatus(
                transactionId,
                principal,
                httpRequest,
                Transaction::block,
                AuditAction.TRANSACTION_BLOCKED,
                "Transaction blocked"
        );
    }

    @Override
    @Transactional
    public TransactionResponse failTransaction(
            UUID transactionId,
            SentinelPayUserPrincipal principal,
            HttpServletRequest httpRequest
    ) {
        return transitionTransactionStatus(
                transactionId,
                principal,
                httpRequest,
                Transaction::fail,
                AuditAction.TRANSACTION_FAILED,
                "Transaction failed"
        );
    }

    @Override
    @Transactional
    public TransactionResponse cancelTransaction(
            UUID transactionId,
            SentinelPayUserPrincipal principal,
            HttpServletRequest httpRequest
    ) {
        Transaction transaction = transactionRepository.findWithDetailsById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + transactionId));

        validateTransactionVisibility(transaction, principal);

        try {
            transaction.cancel();
        } catch (IllegalStateException exception) {
            throw new BadRequestException(exception.getMessage());
        }

        Transaction savedTransaction = transactionRepository.save(transaction);

        recordTransactionStatusAuditEvent(
                savedTransaction,
                principal,
                httpRequest,
                AuditAction.TRANSACTION_CANCELLED,
                "Transaction cancelled"
        );

        return transactionMapper.toTransactionResponse(savedTransaction);
    }


    private TransactionResponse transitionTransactionStatus(
            UUID transactionId,
            SentinelPayUserPrincipal principal,
            HttpServletRequest httpRequest,
            Consumer<Transaction> transitionAction,
            AuditAction auditAction,
            String auditMessage
    ) {
        Transaction transaction = transactionRepository.findWithDetailsById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + transactionId));

        try {
            transitionAction.accept(transaction);
        } catch (IllegalStateException exception) {
            throw new BadRequestException(exception.getMessage());
        }

        Transaction savedTransaction = transactionRepository.save(transaction);

        recordTransactionStatusAuditEvent(
                savedTransaction,
                principal,
                httpRequest,
                auditAction,
                auditMessage
        );

        return transactionMapper.toTransactionResponse(savedTransaction);
    }

    //audit helper
    private void recordTransactionStatusAuditEvent(
            Transaction transaction,
            SentinelPayUserPrincipal principal,
            HttpServletRequest httpRequest,
            AuditAction auditAction,
            String auditMessage
    ) {
        String details = auditMessage
                + " with id "
                + transaction.getId()
                + ", status "
                + transaction.getStatus();

        auditService.recordEvent(new AuditEventCommand(
                principal.getId(),
                auditAction,
                AuditResourceType.TRANSACTION,
                transaction.getId().toString(),
                details,
                requestMetadataUtil.getCorrelationId(httpRequest),
                requestMetadataUtil.getClientIp(httpRequest),
                requestMetadataUtil.getUserAgent(httpRequest)
        ));
    }
}