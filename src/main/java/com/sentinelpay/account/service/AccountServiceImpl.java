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