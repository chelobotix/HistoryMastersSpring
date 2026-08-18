package com.example.historical_figures.security.unit;

import com.example.historical_figures.security.config.AuthenticationFacade;
import com.example.historical_figures.security.config.CustomUserDetails;
import com.example.historical_figures.shared.exceptions.DomainException;
import com.example.historical_figures.user.User;
import com.example.historical_figures.user.UserErrorCodes;
import com.example.historical_figures.user.UserLocaleSupportedLanguages;
import com.example.historical_figures.user.support.UserFixtures;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Locale;

import static com.example.historical_figures.security.config.AuthenticationFacade.ANONYMOUS_LOCALE_SESSION_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AuthenticationFacade")
class AuthenticationFacadeTest {

    private final AuthenticationFacade facade = new AuthenticationFacade();

    private static CustomUserDetails authenticatedPrincipal(User user) {
        return new CustomUserDetails(user);
    }

    private void authenticateAs(User user) {
        CustomUserDetails principal = authenticatedPrincipal(user);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @BeforeEach
    void clearBefore() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void clearAfter() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    private MockHttpServletRequest bindRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        return request;
    }

    @Nested
    @DisplayName("isAuthenticated")
    class IsAuthenticated {

        @Test
        @DisplayName("is false when there is no authentication in the context")
        void falseWhenNoAuthentication() {
            assertThat(facade.isAuthenticated()).isFalse();
        }

        @Test
        @DisplayName("is false for an anonymous authentication token")
        void falseWhenAnonymous() {
            AnonymousAuthenticationToken anonymous = new AnonymousAuthenticationToken(
                    "key", "anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
            SecurityContextHolder.getContext().setAuthentication(anonymous);

            assertThat(facade.isAuthenticated()).isFalse();
        }

        @Test
        @DisplayName("is true for an authenticated user")
        void trueWhenAuthenticated() {
            authenticateAs(UserFixtures.user(1L, UserFixtures.DEFAULT_EMAIL));

            assertThat(facade.isAuthenticated()).isTrue();
        }
    }

    @Nested
    @DisplayName("getCurrentUser")
    class GetCurrentUser {

        @Test
        @DisplayName("throws USER_NOT_LOGGED_IN when nobody is authenticated")
        void throwsWhenNotAuthenticated() {
            assertThatThrownBy(() -> facade.getCurrentUser())
                    .isInstanceOf(DomainException.class)
                    .satisfies(ex -> assertThat(((DomainException) ex).getErrorCode())
                            .isEqualTo(UserErrorCodes.USER_NOT_LOGGED_IN));
        }

        @Test
        @DisplayName("returns the user carried by the principal when authenticated")
        void returnsUserWhenAuthenticated() {
            User user = UserFixtures.user(42L, UserFixtures.DEFAULT_EMAIL);
            authenticateAs(user);

            assertThat(facade.getCurrentUser()).isSameAs(user);
        }
    }

    @Nested
    @DisplayName("getLocaleFromCurrentUser")
    class GetLocaleFromCurrentUser {

        @Test
        @DisplayName("throws USER_NOT_LOGGED_IN when nobody is authenticated")
        void throwsWhenNotAuthenticated() {
            assertThatThrownBy(() -> facade.getLocaleFromCurrentUser())
                    .isInstanceOf(DomainException.class)
                    .satisfies(ex -> assertThat(((DomainException) ex).getErrorCode())
                            .isEqualTo(UserErrorCodes.USER_NOT_LOGGED_IN));
        }

        @Test
        @DisplayName("returns the locale of the authenticated user")
        void returnsLocaleWhenAuthenticated() {
            authenticateAs(UserFixtures.user(1L, UserFixtures.DEFAULT_EMAIL));

            assertThat(facade.getLocaleFromCurrentUser()).isEqualTo(UserLocaleSupportedLanguages.EN);
        }
    }

    @Nested
    @DisplayName("getSessionLocale")
    class GetSessionLocale {

        @Test
        @DisplayName("returns the locale stored in the session")
        void returnsStoredLocale() {
            MockHttpServletRequest request = bindRequest();
            request.getSession(true).setAttribute(ANONYMOUS_LOCALE_SESSION_KEY, Locale.of("es"));

            assertThat(facade.getLocaleFromSession()).contains(Locale.of("es"));
        }

        @Test
        @DisplayName("is empty when the session has no stored locale")
        void emptyWhenNothingStored() {
            bindRequest();

            assertThat(facade.getLocaleFromSession()).isEmpty();
        }

        @Test
        @DisplayName("is empty when there is no bound request")
        void emptyWithoutRequest() {
            assertThat(facade.getLocaleFromSession()).isEmpty();
        }

        @Test
        @DisplayName("is empty when the stored attribute is not a Locale")
        void emptyWhenAttributeIsNotLocale() {
            MockHttpServletRequest request = bindRequest();
            request.getSession(true).setAttribute(ANONYMOUS_LOCALE_SESSION_KEY, "es");

            assertThat(facade.getLocaleFromSession()).isEmpty();
        }
    }
}
