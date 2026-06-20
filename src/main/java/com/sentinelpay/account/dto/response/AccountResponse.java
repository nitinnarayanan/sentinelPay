package com.sentinelpay.account.dto.response;

import com.sentinelpay.account.enums.AccountStatus;
import com.sentinelpay.account.enums.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String accountNumber,
        AccountType accountType,
        AccountStatus status,
        BigDecimal balance,
        String currency,
        LocalDateTime createdAt
) {
}