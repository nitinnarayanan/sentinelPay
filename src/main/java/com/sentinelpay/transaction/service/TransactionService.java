package com.sentinelpay.transaction.service;

import com.sentinelpay.common.security.SentinelPayUserPrincipal;
import com.sentinelpay.transaction.dto.request.CreateTransactionRequest;
import com.sentinelpay.transaction.dto.response.TransactionResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface TransactionService {

    TransactionResponse createTransaction(
            CreateTransactionRequest request,
            SentinelPayUserPrincipal principal,
            HttpServletRequest httpRequest
    );
}