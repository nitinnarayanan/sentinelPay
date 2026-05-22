package com.sentinelpay.user.repository;

import com.sentinelpay.user.entity.AppUser;
import com.sentinelpay.user.enums.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AppUserRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("sentinelpay_test_db")
            .withUsername("sentinelpay_test_user")
            .withPassword("sentinelpay_test_password");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @Autowired
    private AppUserRepository appUserRepository;

    @Test
    @DisplayName("Should save and find user by email")
    void shouldSaveAndFindUserByEmail() {
        AppUser user = new AppUser();
        user.setEmail("repo-test@example.com");
        user.setPasswordHash("hashed-password");
        user.setFirstName("Repo");
        user.setLastName("Test");
        user.setStatus(UserStatus.ACTIVE);

        appUserRepository.save(user);

        Optional<AppUser> result = appUserRepository.findByEmail("repo-test@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("repo-test@example.com");
        assertThat(result.get().getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should return true when email exists")
    void shouldReturnTrueWhenEmailExists() {
        AppUser user = new AppUser();
        user.setEmail("exists@example.com");
        user.setPasswordHash("hashed-password");
        user.setFirstName("Email");
        user.setLastName("Exists");
        user.setStatus(UserStatus.ACTIVE);

        appUserRepository.save(user);

        boolean exists = appUserRepository.existsByEmail("exists@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return empty when email does not exist")
    void shouldReturnEmptyWhenEmailDoesNotExist() {
        Optional<AppUser> result = appUserRepository.findByEmail("missing@example.com");

        assertThat(result).isEmpty();
    }
}