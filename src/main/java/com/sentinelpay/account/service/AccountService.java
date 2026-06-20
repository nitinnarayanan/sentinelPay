package com.sentinelpay.account.service;

import com.sentinelpay.account.dto.request.CreateAccountRequest;
import com.sentinelpay.account.dto.response.AccountResponse;
import com.sentinelpay.common.security.SentinelPayUserPrincipal;

import java.util.List;
import java.util.UUID;

public interface AccountService {

    AccountResponse createAccount(
            CreateAccountRequest request,
            SentinelPayUserPrincipal principal
    );

    List<AccountResponse> getMyAccounts(SentinelPayUserPrincipal principal);

    AccountResponse getMyAccountById(
            UUID accountId,
            SentinelPayUserPrincipal principal
    );
}