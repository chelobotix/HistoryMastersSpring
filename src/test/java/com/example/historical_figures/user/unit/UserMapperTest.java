package com.example.historical_figures.user.unit;

import com.example.historical_figures.user.User;
import com.example.historical_figures.user.UserMapper;
import com.example.historical_figures.user.UserResponse;
import com.example.historical_figures.user.support.UserFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserMapper")
class UserMapperTest {

    @Test
    @DisplayName("maps id, email and role names")
    void mapsUserToResponse() {
        User user = UserFixtures.user(10L, UserFixtures.DEFAULT_EMAIL);

        UserResponse response = UserMapper.toResponse(user);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.email()).isEqualTo(UserFixtures.DEFAULT_EMAIL);
        assertThat(response.roles()).containsExactly(UserFixtures.ROLE_USER);
    }

    @Test
    @DisplayName("maps multiple roles preserving order")
    void mapsMultipleRoles() {
        User user = UserFixtures.userWithRoles(
                1L,
                UserFixtures.DEFAULT_EMAIL,
                List.of(UserFixtures.roleUser(), UserFixtures.roleAdmin())
        );

        UserResponse response = UserMapper.toResponse(user);

        assertThat(response.roles()).containsExactly(UserFixtures.ROLE_USER, UserFixtures.ROLE_ADMIN);
    }

    @Test
    @DisplayName("maps empty roles list")
    void mapsEmptyRoles() {
        User user = UserFixtures.userWithRoles(1L, UserFixtures.DEFAULT_EMAIL, List.of());

        UserResponse response = UserMapper.toResponse(user);

        assertThat(response.roles()).isEmpty();
    }
}
