package com.sentinelpay.common.security;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthorizationTestController {

    @GetMapping("/api/v1/security/customer-area")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Map<String, String> customerArea() {
        return Map.of(
                "message", "Customer role access granted"
        );
    }

    @GetMapping("/api/v1/security/create-transaction-check")
    @PreAuthorize("hasAuthority('TRANSACTION_CREATE')")
    public Map<String, String> transactionCreatePermission() {
        return Map.of(
                "message", "Transaction create permission granted"
        );
    }

    @GetMapping("/api/v1/security/admin-area")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> adminArea() {
        return Map.of(
                "message", "Admin role access granted"
        );
    }

    @GetMapping("/api/v1/security/audit-area")
    @PreAuthorize("hasAuthority('AUDIT_VIEW')")
    public Map<String, String> auditArea() {
        return Map.of(
                "message", "Audit view permission granted"
        );
    }
}