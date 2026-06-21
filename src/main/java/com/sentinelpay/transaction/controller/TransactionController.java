package com.sentinelpay.transaction.controller;

import com.sentinelpay.common.security.SentinelPayUserPrincipal;
import com.sentinelpay.transaction.dto.request.CreateTransactionRequest;
import com.sentinelpay.transaction.dto.response.TransactionResponse;
import com.sentinelpay.transaction.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('TRANSACTION_CREATE')")
    public TransactionResponse createTransaction(
            @Valid @RequestBody CreateTransactionRequest request,
            @AuthenticationPrincipal SentinelPayUserPrincipal principal,
            HttpServletRequest httpRequest
    ) {
        return transactionService.createTransaction(request, principal, httpRequest);
    }
}