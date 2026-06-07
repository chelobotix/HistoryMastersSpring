package com.example.historical_figures.user.integration;

import com.example.historical_figures.role.Role;
import com.example.historical_figures.role.RoleRepository;
import com.example.historical_figures.user.User;
import com.example.historical_figures.user.UserRepository;
import com.example.historical_figures.user.support.UserFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("db")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@Testcontainers
@DisplayName("UserRepository")
class UserRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Role roleUser;

    @BeforeEach
    void setUp() {
        roleUser = roleRepository.findByName(UserFixtures.ROLE_USER)
                .orElseThrow(() -> new IllegalStateException("ROLE_USER seed missing — check Flyway V4"));
    }

    private User newUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(UserFixtures.ENCODED_PASSWORD);
        user.setIsEnabled(true);
        user.setIsLocked(false);
        user.setIsEmailVerified(false);
        user.setRoles(List.of(roleUser));
        return user;
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("persists user and generates id and timestamps")
        void persistsUser() {
            User saved = userRepository.saveAndFlush(newUser(UserFixtures.DEFAULT_EMAIL));

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getEmail()).isEqualTo(UserFixtures.DEFAULT_EMAIL);
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getUpdatedAt()).isNotNull();
            assertThat(userRepository.findById(saved.getId())).isPresent();
        }

        @Test
        @DisplayName("rejects duplicate email via unique constraint")
        void rejectsDuplicateEmail() {
            userRepository.saveAndFlush(newUser(UserFixtures.DEFAULT_EMAIL));

            assertThatThrownBy(() -> userRepository.saveAndFlush(newUser(UserFixtures.DEFAULT_EMAIL)))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Nested
    @DisplayName("findByEmail")
    class FindByEmail {

        @Test
        @DisplayName("returns empty when email does not exist")
        void returnsEmptyWhenMissing() {
            Optional<User> found = userRepository.findByEmail("missing@example.com");

            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("returns user when email exists")
        void returnsUserWhenFound() {
            userRepository.saveAndFlush(newUser(UserFixtures.DEFAULT_EMAIL));

            Optional<User> found = userRepository.findByEmail(UserFixtures.DEFAULT_EMAIL);

            assertThat(found).isPresent();
            assertThat(found.get().getEmail()).isEqualTo(UserFixtures.DEFAULT_EMAIL);
            assertThat(found.get().getPassword()).isEqualTo(UserFixtures.ENCODED_PASSWORD);
        }

        @Test
        @DisplayName("loads roles eagerly via @EntityGraph")
        void loadsRolesViaEntityGraph() {
            userRepository.saveAndFlush(newUser(UserFixtures.DEFAULT_EMAIL));

            User found = userRepository.findByEmail(UserFixtures.DEFAULT_EMAIL).orElseThrow();

            // Outside a fetch join / EntityGraph, roles would be lazy and may fail or be empty proxies.
            // Asserting role data here verifies the @EntityGraph(attributePaths = "roles") works.
            assertThat(found.getRoles())
                    .extracting(Role::getName)
                    .containsExactly(UserFixtures.ROLE_USER);
        }

        @Test
        @DisplayName("is case-sensitive for email lookup")
        void isCaseSensitive() {
            userRepository.saveAndFlush(newUser(UserFixtures.DEFAULT_EMAIL));

            assertThat(userRepository.findByEmail("Ada.Lovelace@Example.COM")).isEmpty();
            assertThat(userRepository.findByEmail(UserFixtures.DEFAULT_EMAIL)).isPresent();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("returns user with roles within the persistence session")
        void returnsUserWithRoles() {
            User saved = userRepository.saveAndFlush(newUser(UserFixtures.DEFAULT_EMAIL));

            User found = userRepository.findById(saved.getId()).orElseThrow();

            assertThat(found.getEmail()).isEqualTo(UserFixtures.DEFAULT_EMAIL);
            assertThat(found.getRoles())
                    .extracting(Role::getName)
                    .containsExactly(UserFixtures.ROLE_USER);
        }
    }

    @Nested
    @DisplayName("user_roles association")
    class UserRolesAssociation {

        @Test
        @DisplayName("persists many-to-many link to ROLE_USER")
        void persistsRoleLink() {
            User saved = userRepository.saveAndFlush(newUser("roles@example.com"));

            User found = userRepository.findByEmail("roles@example.com").orElseThrow();

            assertThat(found.getId()).isEqualTo(saved.getId());
            assertThat(found.getRoles()).hasSize(1);
            assertThat(found.getRoles().getFirst().getId()).isEqualTo(roleUser.getId());
        }
    }
}
