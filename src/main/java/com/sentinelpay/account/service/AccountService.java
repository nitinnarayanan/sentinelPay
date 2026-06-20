package com.sentinelpay.account.service;

import com.sentinelpay.account.dto.request.CreateAccountRequest;
import com.sentinelpay.account.dto.response.AccountResponse;
import com.sentinelpay.common.security.SentinelPayUserPrincipal;

public interface AccountService {

    AccountResponse createAccount(
            CreateAccountRequest request,
            SentinelPayUserPrincipal principal
    );
}