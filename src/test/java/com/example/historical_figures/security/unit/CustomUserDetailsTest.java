package com.example.historical_figures.security.unit;

import com.example.historical_figures.security.config.CustomUserDetails;
import com.example.historical_figures.user.support.UserFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CustomUserDetails")
class CustomUserDetailsTest {

    @Test
    @DisplayName("returns password digest when local credential exists")
    void returnsPasswordWhenLocalCredentialPresent() {
        CustomUserDetails details = new CustomUserDetails(UserFixtures.user(1L, UserFixtures.DEFAULT_EMAIL));

        assertThat(details.getUsername()).isEqualTo(UserFixtures.DEFAULT_EMAIL);
        assertThat(details.getPassword()).isEqualTo(UserFixtures.ENCODED_PASSWORD);
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.isAccountNonLocked()).isTrue();
        assertThat(details.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly(UserFixtures.ROLE_USER);
    }

    @Test
    @DisplayName("returns null password for oauth-only users without local credential")
    void returnsNullPasswordWhenOauthOnly() {
        CustomUserDetails details = new CustomUserDetails(UserFixtures.oauthUser(2L, UserFixtures.DEFAULT_EMAIL));

        assertThat(details.getPassword()).isNull();
        assertThat(details.getUsername()).isEqualTo(UserFixtures.DEFAULT_EMAIL);
        assertThat(details.isEnabled()).isTrue();
    }
}
