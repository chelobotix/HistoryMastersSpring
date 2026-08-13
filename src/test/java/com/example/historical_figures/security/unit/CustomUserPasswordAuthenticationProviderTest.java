package com.example.historical_figures.security.unit;

import com.example.historical_figures.security.config.CustomUserDetails;
import com.example.historical_figures.security.config.CustomUserDetailsService;
import com.example.historical_figures.security.config.CustomUserPasswordAuthenticationProvider;
import com.example.historical_figures.user.support.UserFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserPasswordAuthenticationProvider")
class CustomUserPasswordAuthenticationProviderTest {

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomUserPasswordAuthenticationProvider provider;

    @Test
    @DisplayName("authenticates local user when password matches")
    void authenticatesWhenPasswordMatches() {
        CustomUserDetails details = new CustomUserDetails(UserFixtures.user(1L, UserFixtures.DEFAULT_EMAIL));
        when(customUserDetailsService.loadUserByUsername(UserFixtures.DEFAULT_EMAIL)).thenReturn(details);
        when(passwordEncoder.matches(UserFixtures.DEFAULT_PASSWORD, UserFixtures.ENCODED_PASSWORD)).thenReturn(true);

        Authentication result = provider.authenticate(
                new UsernamePasswordAuthenticationToken(UserFixtures.DEFAULT_EMAIL, UserFixtures.DEFAULT_PASSWORD)
        );

        assertThat(result.getPrincipal()).isSameAs(details);
        assertThat(result.getCredentials()).isNull();
        verify(passwordEncoder).matches(UserFixtures.DEFAULT_PASSWORD, UserFixtures.ENCODED_PASSWORD);
    }

    @Test
    @DisplayName("rejects oauth-only users that have no local password")
    void rejectsOauthOnlyUsers() {
        CustomUserDetails details = new CustomUserDetails(UserFixtures.oauthUser(1L, UserFixtures.DEFAULT_EMAIL));
        when(customUserDetailsService.loadUserByUsername(UserFixtures.DEFAULT_EMAIL)).thenReturn(details);

        assertThatThrownBy(() -> provider.authenticate(
                new UsernamePasswordAuthenticationToken(UserFixtures.DEFAULT_EMAIL, UserFixtures.DEFAULT_PASSWORD)
        )).isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    @DisplayName("maps UsernameNotFoundException to BadCredentialsException")
    void mapsMissingUserToBadCredentials() {
        when(customUserDetailsService.loadUserByUsername(UserFixtures.DEFAULT_EMAIL))
                .thenThrow(new UsernameNotFoundException("User not found"));

        assertThatThrownBy(() -> provider.authenticate(
                new UsernamePasswordAuthenticationToken(UserFixtures.DEFAULT_EMAIL, UserFixtures.DEFAULT_PASSWORD)
        )).isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    @DisplayName("rejects null credentials")
    void rejectsNullCredentials() {
        assertThatThrownBy(() -> provider.authenticate(
                new UsernamePasswordAuthenticationToken(UserFixtures.DEFAULT_EMAIL, null)
        )).isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    @DisplayName("supports UsernamePasswordAuthenticationToken")
    void supportsUsernamePasswordToken() {
        assertThat(provider.supports(UsernamePasswordAuthenticationToken.class)).isTrue();
    }
}
