package com.sentinelpay.transaction.mapper;

import com.sentinelpay.transaction.dto.response.TransactionResponse;
import com.sentinelpay.transaction.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toTransactionResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getSourceAccount().getId(),
                transaction.getSourceAccount().getAccountNumber(),
                transaction.getDestinationAccount().getId(),
                transaction.getDestinationAccount().getAccountNumber(),
                transaction.getTransactionType(),
                transaction.getStatus(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getDescription(),
                transaction.getIdempotencyKey(),
                transaction.getCreatedByUser().getId(),
                transaction.getCreatedAt()
        );
    }
}