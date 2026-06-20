package com.sentinelpay.transaction.repository;

import com.sentinelpay.account.entity.Account;
import com.sentinelpay.transaction.entity.Transaction;
import com.sentinelpay.transaction.enums.TransactionStatus;
import com.sentinelpay.user.entity.AppUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    boolean existsByIdempotencyKey(String idempotencyKey);

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    List<Transaction> findBySourceAccount(Account sourceAccount);

    List<Transaction> findByDestinationAccount(Account destinationAccount);

    List<Transaction> findByCreatedByUser(AppUser createdByUser);

    List<Transaction> findByStatus(TransactionStatus status);

    @EntityGraph(attributePaths = {
            "sourceAccount",
            "destinationAccount",
            "createdByUser"
    })
    Optional<Transaction> findWithDetailsById(UUID id);

    @EntityGraph(attributePaths = {
            "sourceAccount",
            "destinationAccount",
            "createdByUser"
    })
    Optional<Transaction> findWithDetailsByIdempotencyKey(String idempotencyKey);
}