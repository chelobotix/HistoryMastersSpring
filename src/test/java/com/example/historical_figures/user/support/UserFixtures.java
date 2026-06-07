package com.example.historical_figures.user.support;

import com.example.historical_figures.role.Role;
import com.example.historical_figures.user.User;
import com.example.historical_figures.user.UserCreateRequest;

import java.time.Instant;
import java.util.List;

public final class UserFixtures {

    public static final String DEFAULT_EMAIL = "ada.lovelace@example.com";
    public static final String DEFAULT_PASSWORD = "secret12";
    public static final String ENCODED_PASSWORD = "{bcrypt}encoded";
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    private UserFixtures() {
    }

    public static Role roleUser() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        return new Role(1L, ROLE_USER, now, now, null);
    }

    public static User user(Long id, String email) {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        return new User(
                id,
                email,
                ENCODED_PASSWORD,
                true,
                false,
                false,
                null,
                null,
                now,
                now,
                List.of(roleUser())
        );
    }

    public static UserCreateRequest createRequest() {
        return new UserCreateRequest(DEFAULT_EMAIL, DEFAULT_PASSWORD);
    }

    public static UserCreateRequest createRequest(String email, String password) {
        return new UserCreateRequest(email, password);
    }
}
