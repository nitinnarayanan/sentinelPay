package com.sentinelpay.account.repository;

import com.sentinelpay.account.entity.Account;
import com.sentinelpay.account.enums.AccountStatus;
import com.sentinelpay.user.entity.AppUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    List<Account> findByUser(AppUser user);

    List<Account> findByUserAndStatus(AppUser user, AccountStatus status);

    @EntityGraph(attributePaths = {"user"})
    Optional<Account> findWithUserById(UUID id);

    @EntityGraph(attributePaths = {"user"})
    Optional<Account> findWithUserByAccountNumber(String accountNumber);
}