package com.sentinelpay.account.controller;

import com.sentinelpay.account.dto.request.CreateAccountRequest;
import com.sentinelpay.account.dto.response.AccountResponse;
import com.sentinelpay.account.service.AccountService;
import com.sentinelpay.common.security.SentinelPayUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CUSTOMER')")
    public AccountResponse createAccount(
            @Valid @RequestBody CreateAccountRequest request,
            @AuthenticationPrincipal SentinelPayUserPrincipal principal
    ) {
        return accountService.createAccount(request, principal);
    }
}