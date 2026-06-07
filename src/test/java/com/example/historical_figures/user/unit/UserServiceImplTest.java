package com.example.historical_figures.user.unit;

import com.example.historical_figures.role.Role;
import com.example.historical_figures.role.RoleErrorCodes;
import com.example.historical_figures.role.RoleRepository;
import com.example.historical_figures.shared.exceptions.DomainException;
import com.example.historical_figures.user.User;
import com.example.historical_figures.user.UserCreateRequest;
import com.example.historical_figures.user.UserErrorCodes;
import com.example.historical_figures.user.UserRepository;
import com.example.historical_figures.user.UserResponse;
import com.example.historical_figures.user.UserServiceImpl;
import com.example.historical_figures.user.support.UserFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

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
                    .satisfies(ex -> {
                        DomainException domainException = (DomainException) ex;
                        assertThat(domainException.getErrorCode()).isEqualTo(UserErrorCodes.USER_NOT_FOUND);
                    });
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
                    .satisfies(ex -> {
                        DomainException domainException = (DomainException) ex;
                        assertThat(domainException.getErrorCode()).isEqualTo(UserErrorCodes.USER_NOT_FOUND);
                    });
        }
    }

    @Nested
    @DisplayName("save")
    class Save {

        private Role roleUser;

        @BeforeEach
        void setUp() {
            roleUser = UserFixtures.roleUser();
        }

        @Test
        @DisplayName("persists normalized email, encoded password and default role")
        void savesUserSuccessfully() {
            UserCreateRequest request = UserFixtures.createRequest("  Ada.Lovelace@Example.COM ", UserFixtures.DEFAULT_PASSWORD);
            when(passwordEncoder.encode(UserFixtures.DEFAULT_PASSWORD)).thenReturn(UserFixtures.ENCODED_PASSWORD);
            when(roleRepository.findByName(UserFixtures.ROLE_USER)).thenReturn(Optional.of(roleUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UserResponse response = userService.save(request);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User saved = userCaptor.getValue();

            assertThat(saved.getEmail()).isEqualTo("ada.lovelace@example.com");
            assertThat(saved.getPassword()).isEqualTo(UserFixtures.ENCODED_PASSWORD);
            assertThat(saved.getIsEnabled()).isTrue();
            assertThat(saved.getIsLocked()).isFalse();
            assertThat(saved.getRoles()).containsExactly(roleUser);

            assertThat(response.email()).isEqualTo("ada.lovelace@example.com");
            assertThat(response.roles()).containsExactly(UserFixtures.ROLE_USER);
            verify(passwordEncoder).encode(UserFixtures.DEFAULT_PASSWORD);
        }

        @Test
        @DisplayName("throws DomainException when ROLE_USER does not exist")
        void throwsWhenRoleNotFound() {
            when(passwordEncoder.encode(anyString())).thenReturn(UserFixtures.ENCODED_PASSWORD);
            when(roleRepository.findByName(UserFixtures.ROLE_USER)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.save(UserFixtures.createRequest()))
                    .isInstanceOf(DomainException.class)
                    .satisfies(ex -> {
                        DomainException domainException = (DomainException) ex;
                        assertThat(domainException.getErrorCode()).isEqualTo(RoleErrorCodes.ROLE_NOT_FOUND);
                        assertThat(domainException.getDetails()).isEqualTo("Role not found");
                    });

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("throws DomainException when email unique constraint is violated")
        void throwsWhenEmailAlreadyExists() {
            when(passwordEncoder.encode(anyString())).thenReturn(UserFixtures.ENCODED_PASSWORD);
            when(roleRepository.findByName(UserFixtures.ROLE_USER)).thenReturn(Optional.of(roleUser));
            when(userRepository.save(any(User.class))).thenThrow(
                    new DataIntegrityViolationException(
                            "could not execute statement",
                            new RuntimeException("duplicate key value violates unique constraint \"uq_users_email\"")
                    )
            );

            assertThatThrownBy(() -> userService.save(UserFixtures.createRequest()))
                    .isInstanceOf(DomainException.class)
                    .satisfies(ex -> {
                        DomainException domainException = (DomainException) ex;
                        assertThat(domainException.getErrorCode()).isEqualTo(UserErrorCodes.USER_ALREADY_EXISTS);
                    });
        }

        @Test
        @DisplayName("rethrows DataIntegrityViolationException for unrelated constraints")
        void rethrowsUnrelatedConstraintViolation() {
            DataIntegrityViolationException unexpected = new DataIntegrityViolationException(
                    "could not execute statement",
                    new RuntimeException("duplicate key value violates unique constraint \"other_constraint\"")
            );
            when(passwordEncoder.encode(anyString())).thenReturn(UserFixtures.ENCODED_PASSWORD);
            when(roleRepository.findByName(UserFixtures.ROLE_USER)).thenReturn(Optional.of(roleUser));
            when(userRepository.save(any(User.class))).thenThrow(unexpected);

            assertThatThrownBy(() -> userService.save(UserFixtures.createRequest()))
                    .isSameAs(unexpected);
        }
    }
}
