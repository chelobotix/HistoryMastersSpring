package com.example.historical_figures.security.unit;

import com.example.historical_figures.security.config.CustomUserDetails;
import com.example.historical_figures.security.config.CustomUserDetailsService;
import com.example.historical_figures.user.User;
import com.example.historical_figures.user.UserRepository;
import com.example.historical_figures.user.support.UserFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService")
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("loads CustomUserDetails by email")
    void loadsUserByEmail() {
        User user = UserFixtures.user(1L, UserFixtures.DEFAULT_EMAIL);
        when(userRepository.findByEmail(UserFixtures.DEFAULT_EMAIL)).thenReturn(Optional.of(user));

        UserDetails details = customUserDetailsService.loadUserByUsername(UserFixtures.DEFAULT_EMAIL);

        assertThat(details).isInstanceOf(CustomUserDetails.class);
        assertThat(details.getUsername()).isEqualTo(UserFixtures.DEFAULT_EMAIL);
        assertThat(details.getPassword()).isEqualTo(UserFixtures.ENCODED_PASSWORD);
        assertThat(details.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly(UserFixtures.ROLE_USER);
        verify(userRepository).findByEmail(UserFixtures.DEFAULT_EMAIL);
    }

    @Test
    @DisplayName("throws UsernameNotFoundException when email does not exist")
    void throwsWhenUserMissing() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");
    }
}
