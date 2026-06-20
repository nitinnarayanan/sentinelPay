package com.sentinelpay.account.service;

import com.sentinelpay.account.dto.request.CreateAccountRequest;
import com.sentinelpay.account.dto.response.AccountResponse;
import com.sentinelpay.account.entity.Account;
import com.sentinelpay.account.enums.AccountStatus;
import com.sentinelpay.account.mapper.AccountMapper;
import com.sentinelpay.account.repository.AccountRepository;
import com.sentinelpay.common.exception.ResourceNotFoundException;
import com.sentinelpay.common.security.SentinelPayUserPrincipal;
import com.sentinelpay.user.entity.AppUser;
import com.sentinelpay.user.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;

import java.time.LocalDate;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AppUserRepository appUserRepository;
    private final AccountMapper accountMapper;

    @Override
    @Transactional
    public AccountResponse createAccount(
            CreateAccountRequest request,
            SentinelPayUserPrincipal principal
    ) {
        AppUser user = appUserRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + principal.getId()));

        Account account = new Account();
        account.setUser(user);
        account.setAccountNumber(generateUniqueAccountNumber());
        account.setAccountType(request.accountType());
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(request.initialBalance());
        account.setCurrency(request.currency().toUpperCase());

        Account savedAccount = accountRepository.save(account);

        return accountMapper.toAccountResponse(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getMyAccounts(SentinelPayUserPrincipal principal) {
        AppUser user = appUserRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + principal.getId()));

        return accountRepository.findByUser(user)
                .stream()
                .map(accountMapper::toAccountResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getMyAccountById(
            UUID accountId,
            SentinelPayUserPrincipal principal
    ) {
        Account account = accountRepository.findWithUserById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));

        if (!account.getUser().getId().equals(principal.getId())) {
            throw new AccessDeniedException("You do not have permission to access this account");
        }

        return accountMapper.toAccountResponse(account);
    }

    private String generateUniqueAccountNumber() {
        String accountNumber;

        do {
            accountNumber = "ACC-"
                    + LocalDate.now().toString().replace("-", "")
                    + "-"
                    + (100000 + new Random().nextInt(900000));
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }
}