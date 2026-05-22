package com.sentinelpay.common.config;

import com.sentinelpay.auth.repository.PermissionRepository;
import com.sentinelpay.auth.repository.RoleRepository;
import com.sentinelpay.user.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataVerificationRunner implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public void run(String... args) {
        log.info("SentinelPay data verification started");
        log.info("Total users: {}", appUserRepository.count());
        log.info("Total roles: {}", roleRepository.count());
        log.info("Total permissions: {}", permissionRepository.count());
        log.info("SentinelPay data verification completed");
    }
}