package com.example.historical_figures.config;

import com.example.historical_figures.security.config.AuthenticationFacade;
import com.example.historical_figures.user.User;
import com.example.historical_figures.user.UserLocaleSupportedLanguages;
import com.example.historical_figures.user.support.UserFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Locale;
import java.util.Optional;

import static com.example.historical_figures.security.config.AuthenticationFacade.ANONYMOUS_LOCALE_SESSION_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserAwareLocaleResolver")
class UserAwareLocaleResolverTest {

    @Mock
    private AuthenticationFacade authFacade;

    @InjectMocks
    private UserAwareLocaleResolver resolver;

    private final MockHttpServletRequest request = new MockHttpServletRequest();

    @Nested
    @DisplayName("resolveLocale when authenticated")
    class Authenticated {

        @Test
        @DisplayName("returns the locale stored on the user")
        void returnsUserLocale() {
            User user = UserFixtures.user(1L, UserFixtures.DEFAULT_EMAIL);
            user.setLocale(UserLocaleSupportedLanguages.ES);
            when(authFacade.isAuthenticated()).thenReturn(true);
            when(authFacade.getCurrentUser()).thenReturn(user);

            assertThat(resolver.resolveLocale(request)).isEqualTo(Locale.of("es"));
        }
    }

    @Nested
    @DisplayName("resolveLocale when anonymous")
    class Anonymous {

        @Test
        @DisplayName("returns the locale chosen in the session")
        void returnsSessionLocale() {
            when(authFacade.isAuthenticated()).thenReturn(false);
            when(authFacade.getLocaleFromSession()).thenReturn(Optional.of(Locale.of("pt")));

            assertThat(resolver.resolveLocale(request)).isEqualTo(Locale.of("pt"));
        }

        @Test
        @DisplayName("falls back to English when there is no session")
        void fallsBackWithoutSession() {
            when(authFacade.isAuthenticated()).thenReturn(false);

            assertThat(resolver.resolveLocale(request)).isEqualTo(Locale.ENGLISH);
        }
    }

    @Nested
    @DisplayName("setLocale")
    class SetLocale {

        @Test
        @DisplayName("stores the chosen locale in the session")
        void storesLocaleInSession() {
            MockHttpServletResponse response = new MockHttpServletResponse();

            resolver.setLocale(request, response, Locale.of("es"));

            assertThat(request.getSession(false)).isNotNull();
            assertThat(request.getSession(false).getAttribute(ANONYMOUS_LOCALE_SESSION_KEY))
                    .isEqualTo(Locale.of("es"));
        }
    }
}
