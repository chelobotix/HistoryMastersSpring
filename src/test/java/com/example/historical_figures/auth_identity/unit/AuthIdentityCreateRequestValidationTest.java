package com.example.historical_figures.auth_identity.unit;

import com.example.historical_figures.auth_identity.AuthIdentityCreateRequest;
import com.example.historical_figures.auth_identity.AuthIdentityProvider;
import com.example.historical_figures.user.support.UserFixtures;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AuthIdentityCreateRequest validation")
class AuthIdentityCreateRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    private AuthIdentityCreateRequest request(String email, String uid, Boolean emailVerified, String picture) {
        return new AuthIdentityCreateRequest(email, AuthIdentityProvider.GOOGLE, uid, emailVerified, picture);
    }

    private Set<ConstraintViolation<AuthIdentityCreateRequest>> violationsOf(AuthIdentityCreateRequest request) {
        return validator.validate(request);
    }

    @Test
    @DisplayName("accepts a valid google identity request")
    void acceptsValidRequest() {
        assertThat(violationsOf(UserFixtures.googleAuthRequest())).isEmpty();
    }

    @Test
    @DisplayName("allows null picture")
    void allowsNullPicture() {
        assertThat(violationsOf(request(UserFixtures.DEFAULT_EMAIL, UserFixtures.GOOGLE_UID, true, null))).isEmpty();
    }

    @Test
    @DisplayName("accepts emailVerified = false so the service can report a distinct error")
    void acceptsUnverifiedEmailFlag() {
        assertThat(violationsOf(request(UserFixtures.DEFAULT_EMAIL, UserFixtures.GOOGLE_UID, false, null))).isEmpty();
    }

    @Nested
    @DisplayName("email")
    class Email {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t"})
        @DisplayName("rejects blank values")
        void rejectsBlank(String email) {
            assertThat(violationsOf(request(email, UserFixtures.GOOGLE_UID, true, null)))
                    .anyMatch(v -> v.getPropertyPath().toString().equals("email"));
        }

        @Test
        @DisplayName("rejects invalid format")
        void rejectsInvalidFormat() {
            assertThat(violationsOf(request("not-an-email", UserFixtures.GOOGLE_UID, true, null)))
                    .anyMatch(v -> v.getPropertyPath().toString().equals("email"));
        }
    }

    @Nested
    @DisplayName("provider")
    class Provider {

        @Test
        @DisplayName("rejects null provider")
        void rejectsNull() {
            Set<ConstraintViolation<AuthIdentityCreateRequest>> violations = validator.validate(
                    new AuthIdentityCreateRequest(
                            UserFixtures.DEFAULT_EMAIL,
                            null,
                            UserFixtures.GOOGLE_UID,
                            true,
                            null
                    )
            );

            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("provider"));
        }
    }

    @Nested
    @DisplayName("uid")
    class Uid {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t"})
        @DisplayName("rejects blank values")
        void rejectsBlank(String uid) {
            assertThat(violationsOf(request(UserFixtures.DEFAULT_EMAIL, uid, true, null)))
                    .anyMatch(v -> v.getPropertyPath().toString().equals("uid"));
        }
    }

    @Nested
    @DisplayName("emailVerified")
    class EmailVerified {

        @Test
        @DisplayName("rejects null, which is how a missing provider claim arrives")
        void rejectsNull() {
            assertThat(violationsOf(request(UserFixtures.DEFAULT_EMAIL, UserFixtures.GOOGLE_UID, null, null)))
                    .anyMatch(v -> v.getPropertyPath().toString().equals("emailVerified"));
        }
    }
}
