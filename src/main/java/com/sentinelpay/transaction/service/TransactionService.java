package com.sentinelpay.transaction.service;

import com.sentinelpay.common.security.SentinelPayUserPrincipal;
import com.sentinelpay.transaction.dto.request.CreateTransactionRequest;
import com.sentinelpay.transaction.dto.response.TransactionResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.UUID;

public interface TransactionService {

    TransactionResponse createTransaction(
            CreateTransactionRequest request,
            SentinelPayUserPrincipal principal,
            HttpServletRequest httpRequest
    );

    List<TransactionResponse> getMyTransactions(SentinelPayUserPrincipal principal);

    TransactionResponse getMyTransactionById(
            UUID transactionId,
            SentinelPayUserPrincipal principal
    );

    TransactionResponse approveTransaction(
            UUID transactionId,
            SentinelPayUserPrincipal principal,
            HttpServletRequest httpRequest
    );

    TransactionResponse blockTransaction(
            UUID transactionId,
            SentinelPayUserPrincipal principal,
            HttpServletRequest httpRequest
    );

    TransactionResponse failTransaction(
            UUID transactionId,
            SentinelPayUserPrincipal principal,
            HttpServletRequest httpRequest
    );

    TransactionResponse cancelTransaction(
            UUID transactionId,
            SentinelPayUserPrincipal principal,
            HttpServletRequest httpRequest
    );
}