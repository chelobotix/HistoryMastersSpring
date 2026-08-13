package com.example.historical_figures.user.integration;

import com.example.historical_figures.auth_identity.AuthIdentityErrorCodes;
import com.example.historical_figures.auth_identity.AuthIdentityProvider;
import com.example.historical_figures.auth_identity.AuthIdentityRepository;
import com.example.historical_figures.auth_identity.AuthIdentityService;
import com.example.historical_figures.auth_identity.AuthIdentityServiceImpl;
import com.example.historical_figures.local_credential.LocalCredentialCreateRequest;
import com.example.historical_figures.local_credential.LocalCredentialService;
import com.example.historical_figures.local_credential.LocalCredentialServiceImpl;
import com.example.historical_figures.role.RoleServiceImpl;
import com.example.historical_figures.shared.errors.ErrorCode;
import com.example.historical_figures.shared.exceptions.DomainException;
import com.example.historical_figures.user.User;
import com.example.historical_figures.user.UserErrorCodes;
import com.example.historical_figures.user.UserRepository;
import com.example.historical_figures.user.UserResponse;
import com.example.historical_figures.user.UserService;
import com.example.historical_figures.user.UserServiceImpl;
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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("db")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@Import({
        UserServiceImpl.class,
        LocalCredentialServiceImpl.class,
        AuthIdentityServiceImpl.class,
        RoleServiceImpl.class,
        UserServiceImplIntegrationTest.AuthTestBeans.class
})
@Testcontainers
@DisplayName("Account provisioning integration")
class UserServiceImplIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private UserService userService;

    @Autowired
    private LocalCredentialService localCredentialService;

    @Autowired
    private AuthIdentityService authIdentityService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthIdentityRepository authIdentityRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void clearPersistedAccounts() {
        authIdentityRepository.deleteAll();
        userRepository.deleteAll();
    }

    @TestConfiguration
    static class AuthTestBeans {

        @Bean
        PasswordEncoder passwordEncoder() {
            return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        }

        @Bean
        LocalValidatorFactoryBean validator() {
            return new LocalValidatorFactoryBean();
        }
    }

    private void assertDomainError(Throwable thrown, ErrorCode expected) {
        assertThat(thrown).isInstanceOf(DomainException.class);
        assertThat(((DomainException) thrown).getErrorCode()).isEqualTo(expected);
    }

    private OAuth2User googleUser(String email, String uid) {
        return UserFixtures.googleOAuth2User(UserFixtures.googleAttributes(email, uid, Boolean.TRUE));
    }

    @Nested
    @DisplayName("local registration")
    class LocalRegistration {

        @Test
        @DisplayName("persists normalized email, bcrypt digest and ROLE_USER")
        void savesUserEndToEnd() {
            UserResponse response = localCredentialService.saveLocal(
                    new LocalCredentialCreateRequest("  Ada.Lovelace@Example.COM ", UserFixtures.DEFAULT_PASSWORD)
            );

            assertThat(response.id()).isNotNull();
            assertThat(response.email()).isEqualTo(UserFixtures.DEFAULT_EMAIL);
            assertThat(response.roles()).containsExactly(UserFixtures.ROLE_USER);

            User persisted = userRepository.findByEmail(UserFixtures.DEFAULT_EMAIL).orElseThrow();
            assertThat(persisted.getLocalCredential()).isNotNull();
            assertThat(persisted.getLocalCredential().getPasswordDigest()).startsWith("{bcrypt}");
            assertThat(passwordEncoder.matches(
                    UserFixtures.DEFAULT_PASSWORD,
                    persisted.getLocalCredential().getPasswordDigest()
            )).isTrue();
            assertThat(persisted.getIsEnabled()).isTrue();
            assertThat(persisted.getIsLocked()).isFalse();
        }

        @Test
        @DisplayName("rejects a duplicate email with USER_ALREADY_EXISTS")
        void throwsWhenEmailAlreadyExists() {
            localCredentialService.saveLocal(UserFixtures.createRequest());

            assertThatThrownBy(() -> localCredentialService.saveLocal(UserFixtures.createRequest()))
                    .satisfies(ex -> assertDomainError(ex, UserErrorCodes.USER_ALREADY_EXISTS));
        }
    }

    @Nested
    @DisplayName("lookups")
    class Lookups {

        @Test
        @DisplayName("getByEmail returns the mapped response")
        void getByEmailReturnsUser() {
            UserResponse saved = localCredentialService.saveLocal(
                    UserFixtures.createRequest("by-email@example.com", UserFixtures.DEFAULT_PASSWORD)
            );

            UserResponse found = userService.getByEmail("by-email@example.com");

            assertThat(found.id()).isEqualTo(saved.id());
            assertThat(found.roles()).containsExactly(UserFixtures.ROLE_USER);
        }

        @Test
        @DisplayName("getByEmail throws USER_NOT_FOUND for an unknown email")
        void getByEmailThrowsWhenMissing() {
            assertThatThrownBy(() -> userService.getByEmail("missing@example.com"))
                    .satisfies(ex -> assertDomainError(ex, UserErrorCodes.USER_NOT_FOUND));
        }

        @Test
        @DisplayName("getById returns the mapped response including roles")
        void getByIdReturnsUser() {
            UserResponse saved = localCredentialService.saveLocal(
                    UserFixtures.createRequest("by-id@example.com", UserFixtures.DEFAULT_PASSWORD)
            );

            UserResponse found = userService.getById(saved.id());

            assertThat(found.email()).isEqualTo("by-id@example.com");
            assertThat(found.roles()).containsExactly(UserFixtures.ROLE_USER);
        }

        @Test
        @DisplayName("getById throws USER_NOT_FOUND for an unknown id")
        void getByIdThrowsWhenMissing() {
            assertThatThrownBy(() -> userService.getById(999_999L))
                    .satisfies(ex -> assertDomainError(ex, UserErrorCodes.USER_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("google provisioning")
    class GoogleProvisioning {

        @Test
        @DisplayName("creates an oauth-only account with a google identity and ROLE_USER")
        void createsOauthOnlyUser() {
            authIdentityService.oauth2Factory(
                    googleUser("  Ada.Oauth@Example.COM ", "google-sub-new"),
                    AuthIdentityProvider.GOOGLE
            );

            User persisted = userRepository.findByEmail("ada.oauth@example.com").orElseThrow();
            assertThat(persisted.getLocalCredential()).isNull();
            assertThat(persisted.getIsEnabled()).isTrue();
            assertThat(persisted.getIsLocked()).isFalse();
            assertThat(persisted.getIsEmailVerified()).isTrue();
            assertThat(persisted.getRoles())
                    .extracting(role -> role.getName())
                    .containsExactly(UserFixtures.ROLE_USER);

            var identity = authIdentityRepository
                    .findByProviderAndUid(AuthIdentityProvider.GOOGLE, "google-sub-new")
                    .orElseThrow();
            assertThat(identity.getUser().getId()).isEqualTo(persisted.getId());
            assertThat(identity.getPicture()).isEqualTo(UserFixtures.GOOGLE_PICTURE);
        }

        @Test
        @DisplayName("is idempotent for a returning user with the same provider+uid")
        void isIdempotentForReturningUser() {
            OAuth2User oauth2User = googleUser("repeat@example.com", "google-sub-repeat");

            authIdentityService.oauth2Factory(oauth2User, AuthIdentityProvider.GOOGLE);
            // The second login must be a no-op: the account row now exists, and treating
            // that as an email collision would lock the user out permanently.
            authIdentityService.oauth2Factory(oauth2User, AuthIdentityProvider.GOOGLE);

            assertThat(authIdentityRepository.count()).isEqualTo(1);
            assertThat(userRepository.findByEmail("repeat@example.com")).isPresent();
        }

        @Test
        @DisplayName("rejects an email that already belongs to a local account")
        void rejectsEmailOwnedByLocalAccount() {
            localCredentialService.saveLocal(
                    UserFixtures.createRequest("link-me@example.com", UserFixtures.DEFAULT_PASSWORD)
            );

            assertThatThrownBy(() -> authIdentityService.oauth2Factory(
                    googleUser("link-me@example.com", "google-sub-link"),
                    AuthIdentityProvider.GOOGLE
            )).satisfies(ex -> assertDomainError(ex, UserErrorCodes.USER_ALREADY_EXISTS));

            assertThat(authIdentityRepository.findByProviderAndUid(
                    AuthIdentityProvider.GOOGLE, "google-sub-link"
            )).isEmpty();
        }

        @Test
        @DisplayName("rejects an unverified google email")
        void rejectsUnverifiedEmail() {
            OAuth2User oauth2User = UserFixtures.googleOAuth2User(
                    UserFixtures.googleAttributes("unverified@example.com", "google-sub-unverified", Boolean.FALSE)
            );

            assertThatThrownBy(() -> authIdentityService.oauth2Factory(oauth2User, AuthIdentityProvider.GOOGLE))
                    .satisfies(ex -> assertDomainError(
                            ex, AuthIdentityErrorCodes.AUTH_IDENTITY_EMAIL_NOT_VERIFIED));

            assertThat(userRepository.findByEmail("unverified@example.com")).isEmpty();
        }

        @Test
        @DisplayName("rejects a claim set without email_verified")
        void rejectsMissingEmailVerifiedClaim() {
            Map<String, Object> attributes =
                    UserFixtures.googleAttributes("no-claim@example.com", "google-sub-no-claim", null);
            attributes.remove("email_verified");

            assertThatThrownBy(() -> authIdentityService.oauth2Factory(
                    UserFixtures.googleOAuth2User(attributes), AuthIdentityProvider.GOOGLE
            )).satisfies(ex -> assertDomainError(
                    ex, AuthIdentityErrorCodes.AUTH_IDENTITY_INCONSISTENT_PROVIDER_DATA));

            assertThat(userRepository.findByEmail("no-claim@example.com")).isEmpty();
        }
    }
}
