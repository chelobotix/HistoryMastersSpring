package com.example.historical_figures.local_credential.unit;

import com.example.historical_figures.local_credential.LocalCredential;
import com.example.historical_figures.local_credential.LocalCredentialCreateRequest;
import com.example.historical_figures.local_credential.LocalCredentialRepository;
import com.example.historical_figures.local_credential.LocalCredentialServiceImpl;
import com.example.historical_figures.role.RoleErrorCodes;
import com.example.historical_figures.role.RoleName;
import com.example.historical_figures.role.RoleService;
import com.example.historical_figures.shared.exceptions.DomainException;
import com.example.historical_figures.user.User;
import com.example.historical_figures.user.UserErrorCodes;
import com.example.historical_figures.user.UserRepository;
import com.example.historical_figures.user.UserResponse;
import com.example.historical_figures.user.support.UserFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LocalCredentialServiceImpl")
class LocalCredentialServiceImplTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleService roleService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LocalCredentialRepository localCredentialRepository;

    @InjectMocks
    private LocalCredentialServiceImpl localCredentialService;

    /** RoleService mutates the entity in place; the real bean is covered by its own test. */
    private void grantRoleUser() {
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setRoles(new ArrayList<>(List.of(UserFixtures.roleUser())));
            return null;
        }).when(roleService).addRoles(any(User.class), anyCollection());
    }

    @Test
    @DisplayName("persists normalized email, encoded password and ROLE_USER")
    void savesUserSuccessfully() {
        LocalCredentialCreateRequest request =
                UserFixtures.createRequest("  Ada.Lovelace@Example.COM ", UserFixtures.DEFAULT_PASSWORD);
        when(passwordEncoder.encode(UserFixtures.DEFAULT_PASSWORD)).thenReturn(UserFixtures.ENCODED_PASSWORD);
        grantRoleUser();
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(localCredentialRepository.saveAndFlush(any(LocalCredential.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = localCredentialService.saveLocal(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<LocalCredential> credentialCaptor = ArgumentCaptor.forClass(LocalCredential.class);
        verify(userRepository).saveAndFlush(userCaptor.capture());
        verify(localCredentialRepository).saveAndFlush(credentialCaptor.capture());

        User saved = userCaptor.getValue();
        assertThat(saved.getEmail()).isEqualTo("ada.lovelace@example.com");
        assertThat(saved.getIsEnabled()).isTrue();
        assertThat(saved.getIsLocked()).isFalse();

        LocalCredential savedCredential = credentialCaptor.getValue();
        assertThat(savedCredential.getPasswordDigest()).isEqualTo(UserFixtures.ENCODED_PASSWORD);
        assertThat(savedCredential.getUser()).isSameAs(saved);

        assertThat(response.email()).isEqualTo("ada.lovelace@example.com");
        assertThat(response.roles()).containsExactly(UserFixtures.ROLE_USER);
    }

    @Test
    @DisplayName("requests ROLE_USER through RoleService")
    void requestsRoleUser() {
        when(passwordEncoder.encode(anyString())).thenReturn(UserFixtures.ENCODED_PASSWORD);
        grantRoleUser();
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(localCredentialRepository.saveAndFlush(any(LocalCredential.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        localCredentialService.saveLocal(UserFixtures.createRequest());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<RoleName>> rolesCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(roleService).addRoles(any(User.class), rolesCaptor.capture());
        assertThat(rolesCaptor.getValue()).containsExactly(RoleName.ROLE_USER);
    }

    @Test
    @DisplayName("propagates ROLE_NOT_FOUND without persisting anything")
    void propagatesRoleNotFound() {
        when(passwordEncoder.encode(anyString())).thenReturn(UserFixtures.ENCODED_PASSWORD);
        doThrow(new DomainException(RoleErrorCodes.ROLE_NOT_FOUND))
                .when(roleService).addRoles(any(User.class), anyCollection());

        assertThatThrownBy(() -> localCredentialService.saveLocal(UserFixtures.createRequest()))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getErrorCode())
                        .isEqualTo(RoleErrorCodes.ROLE_NOT_FOUND));

        verify(userRepository, never()).saveAndFlush(any(User.class));
        verify(localCredentialRepository, never()).saveAndFlush(any(LocalCredential.class));
    }

    @Test
    @DisplayName("maps uq_users_email violation to USER_ALREADY_EXISTS")
    void mapsEmailConstraintToDomainException() {
        when(passwordEncoder.encode(anyString())).thenReturn(UserFixtures.ENCODED_PASSWORD);
        grantRoleUser();
        when(userRepository.saveAndFlush(any(User.class))).thenThrow(violation("uq_users_email"));

        assertThatThrownBy(() -> localCredentialService.saveLocal(UserFixtures.createRequest()))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getErrorCode())
                        .isEqualTo(UserErrorCodes.USER_ALREADY_EXISTS));

        verify(localCredentialRepository, never()).saveAndFlush(any(LocalCredential.class));
    }

    @Test
    @DisplayName("rethrows unrelated constraint violations untouched")
    void rethrowsUnrelatedConstraintViolation() {
        DataIntegrityViolationException unexpected = violation("some_other_constraint");
        when(passwordEncoder.encode(anyString())).thenReturn(UserFixtures.ENCODED_PASSWORD);
        grantRoleUser();
        when(userRepository.saveAndFlush(any(User.class))).thenThrow(unexpected);

        assertThatThrownBy(() -> localCredentialService.saveLocal(UserFixtures.createRequest()))
                .isSameAs(unexpected);
    }

    private DataIntegrityViolationException violation(String constraint) {
        return new DataIntegrityViolationException(
                "could not execute statement",
                new RuntimeException("duplicate key value violates unique constraint \"" + constraint + "\"")
        );
    }
}
