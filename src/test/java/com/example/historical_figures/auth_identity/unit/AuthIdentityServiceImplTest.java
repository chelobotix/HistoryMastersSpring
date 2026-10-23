package com.example.historical_figures.auth_identity.unit;

import com.example.historical_figures.auth_identity.AuthIdentity;
import com.example.historical_figures.auth_identity.AuthIdentityErrorCodes;
import com.example.historical_figures.auth_identity.AuthIdentityProvider;
import com.example.historical_figures.auth_identity.AuthIdentityRepository;
import com.example.historical_figures.auth_identity.AuthIdentityServiceImpl;
import com.example.historical_figures.role.RoleName;
import com.example.historical_figures.role.RoleService;
import com.example.historical_figures.security.config.AuthenticationFacade;
import com.example.historical_figures.shared.errors.ErrorCode;
import com.example.historical_figures.shared.exceptions.DomainException;
import com.example.historical_figures.user.User;
import com.example.historical_figures.user.UserErrorCodes;
import com.example.historical_figures.user.UserRepository;
import com.example.historical_figures.user.support.UserFixtures;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthIdentityServiceImpl")
class AuthIdentityServiceImplTest {

    @Mock
    private AuthIdentityRepository authIdentityRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private AuthenticationFacade authenticationFacade;

    private static Validator validator;

    private AuthIdentityServiceImpl authIdentityService;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @BeforeEach
    void setUp() {
        // Wired by hand so the real Bean Validation engine runs instead of a mock.
        authIdentityService = new AuthIdentityServiceImpl(
                authIdentityRepository,
                userRepository,
                roleService,
                validator,
                authenticationFacade
        );
    }

    private void assertDomainError(Throwable thrown, ErrorCode expected) {
        assertThat(thrown).isInstanceOf(DomainException.class);
        assertThat(((DomainException) thrown).getErrorCode()).isEqualTo(expected);
    }

    private DataIntegrityViolationException violation(String constraint) {
        return new DataIntegrityViolationException(
                "could not execute statement",
                new RuntimeException("duplicate key value violates unique constraint \"" + constraint + "\"")
        );
    }

    private void identityNotFound() {
        when(authIdentityRepository.findByProviderAndUid(any(), anyString())).thenReturn(Optional.empty());
    }

    @Nested
    @DisplayName("returning users")
    class ReturningUsers {

        @Test
        @DisplayName("does nothing when provider+uid is already provisioned")
        void skipsProvisioningForKnownIdentity() {
            User existing = UserFixtures.oauthUser(7L, UserFixtures.DEFAULT_EMAIL);
            when(authIdentityRepository.findByProviderAndUid(AuthIdentityProvider.GOOGLE, UserFixtures.GOOGLE_UID))
                    .thenReturn(Optional.of(UserFixtures.authIdentity(3L, existing)));

            authIdentityService.oauth2Factory(UserFixtures.googleOAuth2User(), AuthIdentityProvider.GOOGLE);

            // The account row already exists for a returning user, so looking it up by email
            // and treating it as a collision would lock everyone out after the first login.
            verify(userRepository, never()).findByEmail(anyString());
            verify(userRepository, never()).saveAndFlush(any(User.class));
            verify(authIdentityRepository, never()).saveAndFlush(any(AuthIdentity.class));
            verify(roleService, never()).addRoles(any(User.class), anyCollection());
        }

        @Test
        @DisplayName("accepts a known identity even when the email is no longer verified")
        void skipsVerificationForKnownIdentity() {
            User existing = UserFixtures.oauthUser(7L, UserFixtures.DEFAULT_EMAIL);
            when(authIdentityRepository.findByProviderAndUid(AuthIdentityProvider.GOOGLE, UserFixtures.GOOGLE_UID))
                    .thenReturn(Optional.of(UserFixtures.authIdentity(3L, existing)));
            OAuth2User oauth2User = UserFixtures.googleOAuth2User(
                    UserFixtures.googleAttributes(UserFixtures.DEFAULT_EMAIL, UserFixtures.GOOGLE_UID, Boolean.FALSE)
            );

            authIdentityService.oauth2Factory(oauth2User, AuthIdentityProvider.GOOGLE);

            verify(userRepository, never()).saveAndFlush(any(User.class));
        }
    }

    @Nested
    @DisplayName("first login")
    class FirstLogin {

        @Test
        @DisplayName("creates the user and the google identity")
        void createsUserAndIdentity() {
            identityNotFound();
            when(userRepository.findByEmail(UserFixtures.DEFAULT_EMAIL)).thenReturn(Optional.empty());
            when(userRepository.saveAndFlush(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(authIdentityRepository.saveAndFlush(any(AuthIdentity.class))).thenAnswer(inv -> inv.getArgument(0));

            authIdentityService.oauth2Factory(UserFixtures.googleOAuth2User(), AuthIdentityProvider.GOOGLE);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            ArgumentCaptor<AuthIdentity> identityCaptor = ArgumentCaptor.forClass(AuthIdentity.class);
            verify(userRepository).saveAndFlush(userCaptor.capture());
            verify(authIdentityRepository).saveAndFlush(identityCaptor.capture());

            User saved = userCaptor.getValue();
            assertThat(saved.getEmail()).isEqualTo(UserFixtures.DEFAULT_EMAIL);
            assertThat(saved.getIsEnabled()).isTrue();
            assertThat(saved.getIsLocked()).isFalse();
            assertThat(saved.getIsEmailVerified()).isTrue();
            assertThat(saved.getLocalCredential()).isNull();

            AuthIdentity identity = identityCaptor.getValue();
            assertThat(identity.getProvider()).isEqualTo(AuthIdentityProvider.GOOGLE);
            assertThat(identity.getUid()).isEqualTo(UserFixtures.GOOGLE_UID);
            assertThat(identity.getPicture()).isEqualTo(UserFixtures.GOOGLE_PICTURE);
            assertThat(identity.getUser()).isSameAs(saved);
        }

        @Test
        @DisplayName("grants ROLE_USER to the new account")
        void grantsRoleUser() {
            identityNotFound();
            when(userRepository.findByEmail(UserFixtures.DEFAULT_EMAIL)).thenReturn(Optional.empty());
            when(userRepository.saveAndFlush(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(authIdentityRepository.saveAndFlush(any(AuthIdentity.class))).thenAnswer(inv -> inv.getArgument(0));

            authIdentityService.oauth2Factory(UserFixtures.googleOAuth2User(), AuthIdentityProvider.GOOGLE);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<Collection<RoleName>> rolesCaptor = ArgumentCaptor.forClass(Collection.class);
            verify(roleService).addRoles(any(User.class), rolesCaptor.capture());
            assertThat(rolesCaptor.getValue()).containsExactly(RoleName.ROLE_USER);
        }

        @Test
        @DisplayName("normalizes the google email before lookup and persistence")
        void normalizesEmail() {
            identityNotFound();
            when(userRepository.findByEmail(UserFixtures.DEFAULT_EMAIL)).thenReturn(Optional.empty());
            when(userRepository.saveAndFlush(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(authIdentityRepository.saveAndFlush(any(AuthIdentity.class))).thenAnswer(inv -> inv.getArgument(0));
            OAuth2User oauth2User = UserFixtures.googleOAuth2User(
                    UserFixtures.googleAttributes("  Ada.Lovelace@Example.COM ", UserFixtures.GOOGLE_UID, Boolean.TRUE)
            );

            authIdentityService.oauth2Factory(oauth2User, AuthIdentityProvider.GOOGLE);

            verify(userRepository).findByEmail(UserFixtures.DEFAULT_EMAIL);
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).saveAndFlush(userCaptor.capture());
            assertThat(userCaptor.getValue().getEmail()).isEqualTo(UserFixtures.DEFAULT_EMAIL);
        }

        @Test
        @DisplayName("accepts email_verified delivered as a string")
        void acceptsStringEmailVerifiedClaim() {
            identityNotFound();
            when(userRepository.findByEmail(UserFixtures.DEFAULT_EMAIL)).thenReturn(Optional.empty());
            when(userRepository.saveAndFlush(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(authIdentityRepository.saveAndFlush(any(AuthIdentity.class))).thenAnswer(inv -> inv.getArgument(0));
            OAuth2User oauth2User = UserFixtures.googleOAuth2User(
                    UserFixtures.googleAttributes(UserFixtures.DEFAULT_EMAIL, UserFixtures.GOOGLE_UID, "true")
            );

            authIdentityService.oauth2Factory(oauth2User, AuthIdentityProvider.GOOGLE);

            verify(userRepository).saveAndFlush(any(User.class));
        }
    }

    @Nested
    @DisplayName("rejections")
    class Rejections {

        @Test
        @DisplayName("throws PROVIDER_NOT_ALLOWED for an unsupported provider")
        void rejectsUnsupportedProvider() {
            assertThatThrownBy(() -> authIdentityService.oauth2Factory(UserFixtures.googleOAuth2User(), null))
                    .satisfies(ex -> assertDomainError(ex, AuthIdentityErrorCodes.AUTH_IDENTITY_PROVIDER_NOT_ALLOWED));

            verify(authIdentityRepository, never()).findByProviderAndUid(any(), anyString());
        }

        @Test
        @DisplayName("throws EMAIL_NOT_VERIFIED when google reports an unverified email")
        void rejectsUnverifiedEmail() {
            identityNotFound();
            OAuth2User oauth2User = UserFixtures.googleOAuth2User(
                    UserFixtures.googleAttributes(UserFixtures.DEFAULT_EMAIL, UserFixtures.GOOGLE_UID, Boolean.FALSE)
            );

            assertThatThrownBy(() -> authIdentityService.oauth2Factory(oauth2User, AuthIdentityProvider.GOOGLE))
                    .satisfies(ex -> assertDomainError(ex, AuthIdentityErrorCodes.AUTH_IDENTITY_EMAIL_NOT_VERIFIED));

            verify(userRepository, never()).saveAndFlush(any(User.class));
        }

        @Test
        @DisplayName("throws USER_ALREADY_EXISTS when the email belongs to another account")
        void rejectsEmailOwnedByAnotherAccount() {
            identityNotFound();
            when(userRepository.findByEmail(UserFixtures.DEFAULT_EMAIL))
                    .thenReturn(Optional.of(UserFixtures.user(11L, UserFixtures.DEFAULT_EMAIL)));

            assertThatThrownBy(() -> authIdentityService.oauth2Factory(
                    UserFixtures.googleOAuth2User(), AuthIdentityProvider.GOOGLE))
                    .satisfies(ex -> assertDomainError(ex, UserErrorCodes.USER_ALREADY_EXISTS));

            verify(userRepository, never()).saveAndFlush(any(User.class));
            verify(authIdentityRepository, never()).saveAndFlush(any(AuthIdentity.class));
        }

        @Test
        @DisplayName("throws INCONSISTENT_PROVIDER_DATA when the email_verified claim is absent")
        void rejectsMissingEmailVerifiedClaim() {
            Map<String, Object> attributes = UserFixtures.googleAttributes();
            attributes.remove("email_verified");

            assertThatThrownBy(() -> authIdentityService.oauth2Factory(
                    UserFixtures.googleOAuth2User(attributes), AuthIdentityProvider.GOOGLE))
                    .satisfies(ex -> assertDomainError(
                            ex, AuthIdentityErrorCodes.AUTH_IDENTITY_INCONSISTENT_PROVIDER_DATA));

            verify(authIdentityRepository, never()).findByProviderAndUid(any(), anyString());
        }

        @Test
        @DisplayName("throws INCONSISTENT_PROVIDER_DATA when the email claim is absent")
        void rejectsMissingEmail() {
            OAuth2User oauth2User = UserFixtures.googleOAuth2User(
                    UserFixtures.googleAttributes(null, UserFixtures.GOOGLE_UID, Boolean.TRUE)
            );

            assertThatThrownBy(() -> authIdentityService.oauth2Factory(oauth2User, AuthIdentityProvider.GOOGLE))
                    .satisfies(ex -> assertDomainError(
                            ex, AuthIdentityErrorCodes.AUTH_IDENTITY_INCONSISTENT_PROVIDER_DATA));
        }

        @Test
        @DisplayName("throws INCONSISTENT_PROVIDER_DATA when the email claim is not an email")
        void rejectsMalformedEmail() {
            OAuth2User oauth2User = UserFixtures.googleOAuth2User(
                    UserFixtures.googleAttributes("not-an-email", UserFixtures.GOOGLE_UID, Boolean.TRUE)
            );

            assertThatThrownBy(() -> authIdentityService.oauth2Factory(oauth2User, AuthIdentityProvider.GOOGLE))
                    .satisfies(ex -> assertDomainError(
                            ex, AuthIdentityErrorCodes.AUTH_IDENTITY_INCONSISTENT_PROVIDER_DATA));
        }

        @Test
        @DisplayName("throws INCONSISTENT_PROVIDER_DATA when the sub claim is blank")
        void rejectsBlankUid() {
            OAuth2User oauth2User = UserFixtures.googleOAuth2User(
                    UserFixtures.googleAttributes(UserFixtures.DEFAULT_EMAIL, "  ", Boolean.TRUE)
            );

            assertThatThrownBy(() -> authIdentityService.oauth2Factory(oauth2User, AuthIdentityProvider.GOOGLE))
                    .satisfies(ex -> assertDomainError(
                            ex, AuthIdentityErrorCodes.AUTH_IDENTITY_INCONSISTENT_PROVIDER_DATA));
        }
    }

    @Nested
    @DisplayName("constraint violations")
    class ConstraintViolations {

        @BeforeEach
        void arrangeCreatePath() {
            identityNotFound();
            when(userRepository.findByEmail(UserFixtures.DEFAULT_EMAIL)).thenReturn(Optional.empty());
        }

        @Test
        @DisplayName("maps uq_users_email to USER_ALREADY_EXISTS")
        void mapsEmailConstraint() {
            when(userRepository.saveAndFlush(any(User.class))).thenThrow(violation("uq_users_email"));

            assertThatThrownBy(() -> authIdentityService.oauth2Factory(
                    UserFixtures.googleOAuth2User(), AuthIdentityProvider.GOOGLE))
                    .satisfies(ex -> assertDomainError(ex, UserErrorCodes.USER_ALREADY_EXISTS));
        }

        @Test
        @DisplayName("maps uq_auth_identities_provider_uid to USER_ALREADY_EXISTS")
        void mapsProviderUidConstraint() {
            when(userRepository.saveAndFlush(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(authIdentityRepository.saveAndFlush(any(AuthIdentity.class)))
                    .thenThrow(violation("uq_auth_identities_provider_uid"));

            assertThatThrownBy(() -> authIdentityService.oauth2Factory(
                    UserFixtures.googleOAuth2User(), AuthIdentityProvider.GOOGLE))
                    .satisfies(ex -> assertDomainError(ex, UserErrorCodes.USER_ALREADY_EXISTS));
        }

        @Test
        @DisplayName("maps uq_auth_identities_user_provider to PROVIDER_ALREADY_LINKED")
        void mapsUserProviderConstraint() {
            when(userRepository.saveAndFlush(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(authIdentityRepository.saveAndFlush(any(AuthIdentity.class)))
                    .thenThrow(violation("uq_auth_identities_user_provider"));

            assertThatThrownBy(() -> authIdentityService.oauth2Factory(
                    UserFixtures.googleOAuth2User(), AuthIdentityProvider.GOOGLE))
                    .satisfies(ex -> assertDomainError(
                            ex, AuthIdentityErrorCodes.AUTH_IDENTITY_PROVIDER_ALREADY_LINKED));
        }

        @Test
        @DisplayName("rethrows unrelated violations untouched")
        void rethrowsUnrelated() {
            DataIntegrityViolationException unexpected = violation("some_other_constraint");
            when(userRepository.saveAndFlush(any(User.class))).thenThrow(unexpected);

            assertThatThrownBy(() -> authIdentityService.oauth2Factory(
                    UserFixtures.googleOAuth2User(), AuthIdentityProvider.GOOGLE))
                    .isSameAs(unexpected);
        }
    }
}
