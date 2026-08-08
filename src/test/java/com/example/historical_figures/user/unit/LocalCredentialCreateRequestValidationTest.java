package com.example.historical_figures.user.unit;

import com.example.historical_figures.local_credential.LocalCredentialCreateRequest;
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

@DisplayName("LocalCredentialCreateRequest validation")
class LocalCredentialCreateRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("accepts a valid request")
    void acceptsValidRequest() {
        Set<ConstraintViolation<LocalCredentialCreateRequest>> violations =
                validator.validate(UserFixtures.createRequest());

        assertThat(violations).isEmpty();
    }

    @Nested
    @DisplayName("email")
    class Email {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t"})
        @DisplayName("rejects blank values")
        void rejectsBlank(String email) {
            Set<ConstraintViolation<LocalCredentialCreateRequest>> violations =
                    validator.validate(new LocalCredentialCreateRequest(email, UserFixtures.DEFAULT_PASSWORD));

            assertThat(violations)
                    .anyMatch(v -> v.getPropertyPath().toString().equals("email"));
        }

        @Test
        @DisplayName("rejects invalid format")
        void rejectsInvalidFormat() {
            Set<ConstraintViolation<LocalCredentialCreateRequest>> violations =
                    validator.validate(new LocalCredentialCreateRequest("not-an-email", UserFixtures.DEFAULT_PASSWORD));

            assertThat(violations)
                    .anyMatch(v -> v.getPropertyPath().toString().equals("email"));
        }
    }

    @Nested
    @DisplayName("password")
    class Password {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t"})
        @DisplayName("rejects blank values")
        void rejectsBlank(String password) {
            Set<ConstraintViolation<LocalCredentialCreateRequest>> violations =
                    validator.validate(new LocalCredentialCreateRequest(UserFixtures.DEFAULT_EMAIL, password));

            assertThat(violations)
                    .anyMatch(v -> v.getPropertyPath().toString().equals("password"));
        }

        @Test
        @DisplayName("rejects passwords shorter than 6 characters")
        void rejectsTooShort() {
            Set<ConstraintViolation<LocalCredentialCreateRequest>> violations =
                    validator.validate(new LocalCredentialCreateRequest(UserFixtures.DEFAULT_EMAIL, "12345"));

            assertThat(violations)
                    .anyMatch(v -> v.getPropertyPath().toString().equals("password"));
        }

        @Test
        @DisplayName("rejects passwords longer than 20 characters")
        void rejectsTooLong() {
            Set<ConstraintViolation<LocalCredentialCreateRequest>> violations =
                    validator.validate(new LocalCredentialCreateRequest(UserFixtures.DEFAULT_EMAIL, "a".repeat(21)));

            assertThat(violations)
                    .anyMatch(v -> v.getPropertyPath().toString().equals("password"));
        }

        @Test
        @DisplayName("accepts password at min and max length bounds")
        void acceptsBoundaryLengths() {
            assertThat(validator.validate(new LocalCredentialCreateRequest(UserFixtures.DEFAULT_EMAIL, "123456"))).isEmpty();
            assertThat(validator.validate(new LocalCredentialCreateRequest(UserFixtures.DEFAULT_EMAIL, "a".repeat(20)))).isEmpty();
        }
    }
}
