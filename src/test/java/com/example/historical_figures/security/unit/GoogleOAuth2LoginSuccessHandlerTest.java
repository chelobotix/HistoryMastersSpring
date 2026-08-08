package com.example.historical_figures.security.unit;

import com.example.historical_figures.auth_identity.AuthIdentityErrorCodes;
import com.example.historical_figures.auth_identity.AuthIdentityProvider;
import com.example.historical_figures.auth_identity.AuthIdentityService;
import com.example.historical_figures.security.config.CustomUserDetails;
import com.example.historical_figures.security.config.CustomUserDetailsService;
import com.example.historical_figures.security.config.GoogleOAuth2LoginSuccessHandler;
import com.example.historical_figures.shared.exceptions.DomainException;
import com.example.historical_figures.user.support.UserFixtures;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GoogleOAuth2LoginSuccessHandler")
class GoogleOAuth2LoginSuccessHandlerTest {

    private static final String REJECT_URL = "/login?error=true&type=oauth2";

    @Mock
    private AuthIdentityService authIdentityService;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private SecurityContextRepository securityContextRepository;

    @Mock
    private RequestCache requestCache;

    @Mock
    private Authentication authentication;

    @Mock
    private SavedRequest savedRequest;

    private GoogleOAuth2LoginSuccessHandler handler;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        handler = new GoogleOAuth2LoginSuccessHandler(
                authIdentityService,
                customUserDetailsService,
                securityContextRepository,
                requestCache
        );
        request = new MockHttpServletRequest("GET", "/login/oauth2/code/google");
        response = new MockHttpServletResponse();
        session = new MockHttpSession();
        request.setSession(session);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private OAuth2User givenGooglePrincipal() {
        OAuth2User oauth2User = UserFixtures.googleOAuth2User();
        when(authentication.getPrincipal()).thenReturn(oauth2User);
        return oauth2User;
    }

    @Nested
    @DisplayName("successful login")
    class SuccessfulLogin {

        @Test
        @DisplayName("provisions the account, swaps the principal and persists the context")
        void establishesLocalPrincipal() throws Exception {
            OAuth2User oauth2User = givenGooglePrincipal();
            when(authentication.getDetails()).thenReturn("session-details");
            CustomUserDetails userDetails =
                    new CustomUserDetails(UserFixtures.oauthUser(1L, UserFixtures.DEFAULT_EMAIL));
            when(customUserDetailsService.loadUserByUsername(UserFixtures.DEFAULT_EMAIL)).thenReturn(userDetails);

            handler.onAuthenticationSuccess(request, response, authentication);

            verify(authIdentityService).oauth2Factory(oauth2User, AuthIdentityProvider.GOOGLE);

            ArgumentCaptor<SecurityContext> contextCaptor = ArgumentCaptor.forClass(SecurityContext.class);
            verify(securityContextRepository).saveContext(contextCaptor.capture(), eq(request), eq(response));

            Authentication saved = contextCaptor.getValue().getAuthentication();
            assertThat(saved.getPrincipal()).isSameAs(userDetails);
            assertThat(saved.getCredentials()).isNull();
            assertThat(saved.getDetails()).isEqualTo("session-details");
            assertThat(saved.getAuthorities())
                    .extracting(GrantedAuthority::getAuthority)
                    .containsExactly(UserFixtures.ROLE_USER);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(saved);
            assertThat(response.getRedirectedUrl()).isEqualTo("/home");
        }

        @Test
        @DisplayName("redirects to the cached request instead of the default target")
        void honoursSavedRequest() throws Exception {
            givenGooglePrincipal();
            when(customUserDetailsService.loadUserByUsername(UserFixtures.DEFAULT_EMAIL))
                    .thenReturn(new CustomUserDetails(UserFixtures.oauthUser(1L, UserFixtures.DEFAULT_EMAIL)));
            when(savedRequest.getRedirectUrl()).thenReturn("http://localhost/figures/42");
            when(requestCache.getRequest(request, response)).thenReturn(savedRequest);

            handler.onAuthenticationSuccess(request, response, authentication);

            assertThat(response.getRedirectedUrl()).isEqualTo("http://localhost/figures/42");
        }

        @Test
        @DisplayName("passes the raw google email to CustomUserDetailsService, which normalizes it")
        void delegatesEmailNormalization() throws Exception {
            OAuth2User oauth2User = UserFixtures.googleOAuth2User(
                    UserFixtures.googleAttributes("  Ada.Lovelace@Example.COM ", UserFixtures.GOOGLE_UID, Boolean.TRUE)
            );
            when(authentication.getPrincipal()).thenReturn(oauth2User);
            when(customUserDetailsService.loadUserByUsername("  Ada.Lovelace@Example.COM "))
                    .thenReturn(new CustomUserDetails(UserFixtures.oauthUser(1L, UserFixtures.DEFAULT_EMAIL)));

            handler.onAuthenticationSuccess(request, response, authentication);

            assertThat(response.getRedirectedUrl()).isEqualTo("/home");
        }
    }

    @Nested
    @DisplayName("rejected login")
    class RejectedLogin {

        private void assertRejected() {
            assertThat(response.getRedirectedUrl()).isEqualTo(REJECT_URL);
            assertThat(session.isInvalid()).isTrue();
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(securityContextRepository, never()).saveContext(any(), any(), any());
        }

        @Test
        @DisplayName("rejects when provisioning fails with a DomainException")
        void rejectsProvisioningFailure() throws Exception {
            givenGooglePrincipal();
            doThrow(new DomainException(AuthIdentityErrorCodes.AUTH_IDENTITY_EMAIL_NOT_VERIFIED))
                    .when(authIdentityService).oauth2Factory(any(), any());

            handler.onAuthenticationSuccess(request, response, authentication);

            assertRejected();
            verify(customUserDetailsService, never()).loadUserByUsername(any());
        }

        @Test
        @DisplayName("rejects when the provisioned account cannot be loaded")
        void rejectsMissingAccount() throws Exception {
            givenGooglePrincipal();
            when(customUserDetailsService.loadUserByUsername(UserFixtures.DEFAULT_EMAIL))
                    .thenThrow(new UsernameNotFoundException("User not found"));

            handler.onAuthenticationSuccess(request, response, authentication);

            assertRejected();
        }

        @Test
        @DisplayName("rejects a disabled account")
        void rejectsDisabledAccount() throws Exception {
            givenGooglePrincipal();
            when(customUserDetailsService.loadUserByUsername(UserFixtures.DEFAULT_EMAIL))
                    .thenReturn(new CustomUserDetails(
                            UserFixtures.disabledOauthUser(1L, UserFixtures.DEFAULT_EMAIL)));

            handler.onAuthenticationSuccess(request, response, authentication);

            assertRejected();
        }

        @Test
        @DisplayName("rejects a locked account")
        void rejectsLockedAccount() throws Exception {
            givenGooglePrincipal();
            when(customUserDetailsService.loadUserByUsername(UserFixtures.DEFAULT_EMAIL))
                    .thenReturn(new CustomUserDetails(
                            UserFixtures.lockedOauthUser(1L, UserFixtures.DEFAULT_EMAIL)));

            handler.onAuthenticationSuccess(request, response, authentication);

            assertRejected();
        }
    }
}
