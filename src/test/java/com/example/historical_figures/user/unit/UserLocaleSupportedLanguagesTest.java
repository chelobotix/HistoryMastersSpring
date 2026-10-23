package com.example.historical_figures.user.unit;

import com.example.historical_figures.shared.exceptions.DomainException;
import com.example.historical_figures.user.UserErrorCodes;
import com.example.historical_figures.user.UserLocaleSupportedLanguages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("UserLocaleSupportedLanguages.validate")
class UserLocaleSupportedLanguagesTest {

    @Nested
    @DisplayName("accepts a supported locale")
    class Supported {

        @Test
        @DisplayName("parses a lower-case code")
        void parsesLowerCase() {
            assertThat(UserLocaleSupportedLanguages.validate("es"))
                    .isEqualTo(UserLocaleSupportedLanguages.ES);
        }

        @Test
        @DisplayName("parses an upper-case code")
        void parsesUpperCase() {
            assertThat(UserLocaleSupportedLanguages.validate("PT"))
                    .isEqualTo(UserLocaleSupportedLanguages.PT);
        }

        @Test
        @DisplayName("trims surrounding whitespace before parsing")
        void trimsWhitespace() {
            assertThat(UserLocaleSupportedLanguages.validate("  en  "))
                    .isEqualTo(UserLocaleSupportedLanguages.EN);
        }
    }

    @Nested
    @DisplayName("rejects an unsupported locale")
    class Unsupported {

        @ParameterizedTest
        @ValueSource(strings = {"fr", "de", "zz", ""})
        @DisplayName("throws USER_INVALID_LOCALE for a code outside the enum")
        void throwsForUnknownCode(String code) {
            assertThatThrownBy(() -> UserLocaleSupportedLanguages.validate(code))
                    .isInstanceOf(DomainException.class)
                    .satisfies(ex -> assertThat(((DomainException) ex).getErrorCode())
                            .isEqualTo(UserErrorCodes.USER_INVALID_LOCALE));
        }

        @Test
        @DisplayName("throws USER_INVALID_LOCALE for a null code")
        void throwsForNull() {
            assertThatThrownBy(() -> UserLocaleSupportedLanguages.validate(null))
                    .isInstanceOf(DomainException.class)
                    .satisfies(ex -> assertThat(((DomainException) ex).getErrorCode())
                            .isEqualTo(UserErrorCodes.USER_INVALID_LOCALE));
        }
    }
}
