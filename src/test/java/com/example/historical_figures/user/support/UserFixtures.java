package com.example.historical_figures.user.support;

import com.example.historical_figures.auth_identity.AuthIdentity;
import com.example.historical_figures.auth_identity.AuthIdentityCreateRequest;
import com.example.historical_figures.auth_identity.AuthIdentityProvider;
import com.example.historical_figures.local_credential.LocalCredential;
import com.example.historical_figures.local_credential.LocalCredentialCreateRequest;
import com.example.historical_figures.role.Role;
import com.example.historical_figures.user.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class UserFixtures {

    public static final String DEFAULT_EMAIL = "ada.lovelace@example.com";
    public static final String DEFAULT_PASSWORD = "secret12";
    public static final String ENCODED_PASSWORD = "{bcrypt}encoded";
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String GOOGLE_UID = "google-sub-123";
    public static final String GOOGLE_PICTURE = "https://lh3.googleusercontent.com/a/photo";

    private static final Instant FIXED_NOW = Instant.parse("2026-01-01T00:00:00Z");

    private UserFixtures() {
    }

    public static Role roleUser() {
        return new Role(1L, ROLE_USER, FIXED_NOW, FIXED_NOW, null);
    }

    public static Role roleAdmin() {
        return new Role(2L, ROLE_ADMIN, FIXED_NOW, FIXED_NOW, null);
    }

    public static LocalCredential localCredential(User user) {
        return new LocalCredential(1L, user, ENCODED_PASSWORD, null, null, FIXED_NOW, FIXED_NOW);
    }

    /** Local account: email + password digest. */
    public static User user(Long id, String email) {
        LocalCredential credential = localCredential(null);
        User user = build(id, email, true, false, false, List.of(roleUser()), credential);
        credential.setUser(user);
        return user;
    }

    /** OAuth-only account: no local password credential. */
    public static User oauthUser(Long id, String email) {
        return build(id, email, true, false, true, List.of(roleUser()), null);
    }

    public static User disabledOauthUser(Long id, String email) {
        return build(id, email, false, false, true, List.of(roleUser()), null);
    }

    public static User lockedOauthUser(Long id, String email) {
        return build(id, email, true, true, true, List.of(roleUser()), null);
    }

    public static User userWithRoles(Long id, String email, List<Role> roles) {
        return build(id, email, true, false, false, roles, null);
    }

    private static User build(
            Long id,
            String email,
            boolean enabled,
            boolean locked,
            boolean emailVerified,
            List<Role> roles,
            LocalCredential credential) {
        return new User(
                id,
                email,
                enabled,
                locked,
                emailVerified,
                null,
                FIXED_NOW,
                FIXED_NOW,
                roles,
                credential,
                new ArrayList<>()
        );
    }

    public static AuthIdentity authIdentity(Long id, User user) {
        return authIdentity(id, user, GOOGLE_UID);
    }

    public static AuthIdentity authIdentity(Long id, User user, String uid) {
        return new AuthIdentity(
                id,
                user,
                AuthIdentityProvider.GOOGLE,
                uid,
                GOOGLE_PICTURE,
                FIXED_NOW,
                FIXED_NOW
        );
    }

    public static LocalCredentialCreateRequest createRequest() {
        return new LocalCredentialCreateRequest(DEFAULT_EMAIL, DEFAULT_PASSWORD);
    }

    public static LocalCredentialCreateRequest createRequest(String email, String password) {
        return new LocalCredentialCreateRequest(email, password);
    }

    public static AuthIdentityCreateRequest googleAuthRequest() {
        return googleAuthRequest(DEFAULT_EMAIL, GOOGLE_UID);
    }

    public static AuthIdentityCreateRequest googleAuthRequest(String email, String uid) {
        return new AuthIdentityCreateRequest(
                email,
                AuthIdentityProvider.GOOGLE,
                uid,
                true,
                GOOGLE_PICTURE
        );
    }

    /**
     * Mutable copy of the claim set Google's userinfo endpoint returns, so tests can
     * drop or override individual attributes.
     */
    public static Map<String, Object> googleAttributes() {
        return googleAttributes(DEFAULT_EMAIL, GOOGLE_UID, Boolean.TRUE);
    }

    public static Map<String, Object> googleAttributes(String email, String uid, Object emailVerified) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", email);
        attributes.put("sub", uid);
        attributes.put("picture", GOOGLE_PICTURE);
        attributes.put("email_verified", emailVerified);
        return attributes;
    }

    public static OAuth2User googleOAuth2User() {
        return googleOAuth2User(googleAttributes());
    }

    public static OAuth2User googleOAuth2User(Map<String, Object> attributes) {
        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("OAUTH2_USER")),
                attributes,
                "sub"
        );
    }
}
