package com.example.historical_figures.user.unit;

import com.example.historical_figures.shared.exceptions.DomainException;
import com.example.historical_figures.user.User;
import com.example.historical_figures.user.UserErrorCodes;
import com.example.historical_figures.user.UserRepository;
import com.example.historical_figures.user.UserResponse;
import com.example.historical_figures.user.UserServiceImpl;
import com.example.historical_figures.user.support.UserFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("returns user response when user exists")
        void returnsUserWhenFound() {
            User user = UserFixtures.user(10L, UserFixtures.DEFAULT_EMAIL);
            when(userRepository.findById(10L)).thenReturn(Optional.of(user));

            UserResponse response = userService.getById(10L);

            assertThat(response.id()).isEqualTo(10L);
            assertThat(response.email()).isEqualTo(UserFixtures.DEFAULT_EMAIL);
            assertThat(response.roles()).containsExactly(UserFixtures.ROLE_USER);
            verify(userRepository).findById(10L);
        }

        @Test
        @DisplayName("throws DomainException when user does not exist")
        void throwsWhenNotFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getById(99L))
                    .isInstanceOf(DomainException.class)
                    .satisfies(ex -> assertThat(((DomainException) ex).getErrorCode())
                            .isEqualTo(UserErrorCodes.USER_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("getByEmail")
    class GetByEmail {

        @Test
        @DisplayName("returns user response when user exists")
        void returnsUserWhenFound() {
            User user = UserFixtures.user(5L, UserFixtures.DEFAULT_EMAIL);
            when(userRepository.findByEmail(UserFixtures.DEFAULT_EMAIL)).thenReturn(Optional.of(user));

            UserResponse response = userService.getByEmail(UserFixtures.DEFAULT_EMAIL);

            assertThat(response.id()).isEqualTo(5L);
            assertThat(response.email()).isEqualTo(UserFixtures.DEFAULT_EMAIL);
            assertThat(response.roles()).containsExactly(UserFixtures.ROLE_USER);
            verify(userRepository).findByEmail(UserFixtures.DEFAULT_EMAIL);
        }

        @Test
        @DisplayName("throws DomainException when user does not exist")
        void throwsWhenNotFound() {
            when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getByEmail("missing@example.com"))
                    .isInstanceOf(DomainException.class)
                    .satisfies(ex -> assertThat(((DomainException) ex).getErrorCode())
                            .isEqualTo(UserErrorCodes.USER_NOT_FOUND));
        }
    }
}
