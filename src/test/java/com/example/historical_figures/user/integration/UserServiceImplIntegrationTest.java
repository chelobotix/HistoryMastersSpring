package com.example.historical_figures.user.integration;

import com.example.historical_figures.shared.exceptions.DomainException;
import com.example.historical_figures.user.UserCreateRequest;
import com.example.historical_figures.user.UserErrorCodes;
import com.example.historical_figures.user.UserRepository;
import com.example.historical_figures.user.UserResponse;
import com.example.historical_figures.user.UserService;
import com.example.historical_figures.user.UserServiceImpl;
import com.example.historical_figures.user.support.UserFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("db")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@Import({UserServiceImpl.class, UserServiceImplIntegrationTest.PasswordEncoderTestConfig.class})
@Testcontainers
@DisplayName("UserServiceImpl integration")
class UserServiceImplIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @TestConfiguration
    static class PasswordEncoderTestConfig {
        @Bean
        PasswordEncoder passwordEncoder() {
            return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        }
    }

    @BeforeEach
    void cleanUsers() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("save persists normalized email, encoded password and default role")
    void savesUserEndToEnd() {
        UserResponse response = userService.save(
                new UserCreateRequest("  Ada.Lovelace@Example.COM ", UserFixtures.DEFAULT_PASSWORD)
        );

        assertThat(response.id()).isNotNull();
        assertThat(response.email()).isEqualTo("ada.lovelace@example.com");
        assertThat(response.roles()).containsExactly(UserFixtures.ROLE_USER);

        var persisted = userRepository.findByEmail("ada.lovelace@example.com").orElseThrow();
        assertThat(persisted.getPassword()).startsWith("{bcrypt}");
        assertThat(passwordEncoder.matches(UserFixtures.DEFAULT_PASSWORD, persisted.getPassword())).isTrue();
        assertThat(persisted.getIsEnabled()).isTrue();
        assertThat(persisted.getIsLocked()).isFalse();
        assertThat(persisted.getRoles())
                .extracting(role -> role.getName())
                .containsExactly(UserFixtures.ROLE_USER);
    }

    @Test
    @DisplayName("save throws USER_ALREADY_EXISTS when email unique constraint is violated")
    void throwsWhenEmailAlreadyExists() {
        userService.save(UserFixtures.createRequest());

        assertThatThrownBy(() -> userService.save(UserFixtures.createRequest()))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> {
                    DomainException domainException = (DomainException) ex;
                    assertThat(domainException.getErrorCode()).isEqualTo(UserErrorCodes.USER_ALREADY_EXISTS);
                });
    }

    @Test
    @DisplayName("getByEmail returns mapped response for persisted user")
    void getByEmailReturnsUserWhenFound() {
        UserResponse saved = userService.save(UserFixtures.createRequest("by-email@example.com", UserFixtures.DEFAULT_PASSWORD));

        UserResponse found = userService.getByEmail("by-email@example.com");

        assertThat(found.id()).isEqualTo(saved.id());
        assertThat(found.email()).isEqualTo("by-email@example.com");
        assertThat(found.roles()).containsExactly(UserFixtures.ROLE_USER);
    }

    @Test
    @DisplayName("getByEmail throws USER_NOT_FOUND when email does not exist")
    void getByEmailThrowsWhenMissing() {
        assertThatThrownBy(() -> userService.getByEmail("missing@example.com"))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> {
                    DomainException domainException = (DomainException) ex;
                    assertThat(domainException.getErrorCode()).isEqualTo(UserErrorCodes.USER_NOT_FOUND);
                });
    }

    @Test
    @DisplayName("getById returns mapped response including roles")
    void getByIdReturnsUserWithRoles() {
        UserResponse saved = userService.save(UserFixtures.createRequest("by-id@example.com", UserFixtures.DEFAULT_PASSWORD));

        UserResponse found = userService.getById(saved.id());

        assertThat(found.id()).isEqualTo(saved.id());
        assertThat(found.email()).isEqualTo("by-id@example.com");
        assertThat(found.roles()).containsExactly(UserFixtures.ROLE_USER);
    }

    @Test
    @DisplayName("getById throws USER_NOT_FOUND when id does not exist")
    void getByIdThrowsWhenMissing() {
        assertThatThrownBy(() -> userService.getById(999_999L))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> {
                    DomainException domainException = (DomainException) ex;
                    assertThat(domainException.getErrorCode()).isEqualTo(UserErrorCodes.USER_NOT_FOUND);
                });
    }
}
