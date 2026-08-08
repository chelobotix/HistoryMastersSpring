package com.example.historical_figures.role.unit;

import com.example.historical_figures.role.Role;
import com.example.historical_figures.role.RoleErrorCodes;
import com.example.historical_figures.role.RoleName;
import com.example.historical_figures.role.RoleRepository;
import com.example.historical_figures.role.RoleServiceImpl;
import com.example.historical_figures.shared.exceptions.DomainException;
import com.example.historical_figures.user.User;
import com.example.historical_figures.user.support.UserFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoleServiceImpl")
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    @Test
    @DisplayName("assigns the requested roles to a user without roles")
    void assignsRolesToNewUser() {
        Role roleUser = UserFixtures.roleUser();
        when(roleRepository.findByNameIn(List.of(UserFixtures.ROLE_USER))).thenReturn(List.of(roleUser));
        User user = new User();

        roleService.addRoles(user, List.of(RoleName.ROLE_USER));

        assertThat(user.getRoles()).containsExactly(roleUser);
    }

    @Test
    @DisplayName("keeps existing roles and appends only the missing ones")
    void appendsWithoutReplacing() {
        Role roleUser = UserFixtures.roleUser();
        Role roleAdmin = UserFixtures.roleAdmin();
        when(roleRepository.findByNameIn(List.of(UserFixtures.ROLE_ADMIN))).thenReturn(List.of(roleAdmin));
        User user = new User();
        user.setRoles(new ArrayList<>(List.of(roleUser)));

        roleService.addRoles(user, List.of(RoleName.ROLE_ADMIN));

        assertThat(user.getRoles()).containsExactly(roleUser, roleAdmin);
    }

    @Test
    @DisplayName("collapses duplicated role names instead of reporting them as missing")
    void tolerantToDuplicateRequests() {
        Role roleUser = UserFixtures.roleUser();
        when(roleRepository.findByNameIn(List.of(UserFixtures.ROLE_USER))).thenReturn(List.of(roleUser));
        User user = new User();

        roleService.addRoles(user, List.of(RoleName.ROLE_USER, RoleName.ROLE_USER));

        assertThat(user.getRoles()).containsExactly(roleUser);
    }

    @Test
    @DisplayName("throws ROLE_NOT_FOUND when a requested role is not seeded")
    void throwsWhenRoleMissing() {
        when(roleRepository.findByNameIn(List.of(UserFixtures.ROLE_USER))).thenReturn(List.of());
        User user = new User();

        assertThatThrownBy(() -> roleService.addRoles(user, List.of(RoleName.ROLE_USER)))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getErrorCode())
                        .isEqualTo(RoleErrorCodes.ROLE_NOT_FOUND));

        assertThat(user.getRoles()).isNull();
    }
}
