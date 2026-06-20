package com.sentinelpay.transaction.dto.response;

import com.sentinelpay.transaction.enums.TransactionStatus;
import com.sentinelpay.transaction.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID sourceAccountId,
        String sourceAccountNumber,
        UUID destinationAccountId,
        String destinationAccountNumber,
        TransactionType transactionType,
        TransactionStatus status,
        BigDecimal amount,
        String currency,
        String description,
        String idempotencyKey,
        UUID createdByUserId,
        LocalDateTime createdAt
) {
}