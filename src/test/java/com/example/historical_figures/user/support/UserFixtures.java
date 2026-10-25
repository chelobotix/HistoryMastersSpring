package com.example.historical_figures.user.support;

import com.example.historical_figures.auth_identity.AuthIdentity;
import com.example.historical_figures.auth_identity.AuthIdentityCreateRequest;
import com.example.historical_figures.auth_identity.AuthIdentityProvider;
import com.example.historical_figures.local_credential.LocalCredential;
import com.example.historical_figures.local_credential.LocalCredentialCreateRequest;
import com.example.historical_figures.role.Role;
import com.example.historical_figures.user.User;
import com.example.historical_figures.user.UserLocaleSupportedLanguages;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

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

    private static <T> T withId(T entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }

    public static Role roleUser() {
        return withId(new Role(ROLE_USER, FIXED_NOW, FIXED_NOW, null), 1L);
    }

    public static Role roleAdmin() {
        return withId(new Role(ROLE_ADMIN, FIXED_NOW, FIXED_NOW, null), 2L);
    }

    public static LocalCredential localCredential(User user) {
        return withId(new LocalCredential(user, ENCODED_PASSWORD, null, null, FIXED_NOW, FIXED_NOW), 1L);
    }

    /** Local account: email + password digest. */
    public static User user(Long id, String email) {
        LocalCredential credential = localCredential(null);
        User user = build(id, email, true, false, false, UserLocaleSupportedLanguages.EN, List.of(roleUser()), credential);
        credential.setUser(user);
        return user;
    }

    /** OAuth-only account: no local password credential. */
    public static User oauthUser(Long id, String email) {
        return build(id, email, true, false, true, UserLocaleSupportedLanguages.EN, List.of(roleUser()), null);
    }

    public static User disabledOauthUser(Long id, String email) {
        return build(id, email, false, false, true, UserLocaleSupportedLanguages.EN, List.of(roleUser()), null);
    }

    public static User lockedOauthUser(Long id, String email) {
        return build(id, email, true, true, true, UserLocaleSupportedLanguages.EN, List.of(roleUser()), null);
    }

    public static User userWithRoles(Long id, String email, List<Role> roles) {
        return build(id, email, true, false, false, UserLocaleSupportedLanguages.EN, roles, null);
    }

    private static User build(
            Long id,
            String email,
            boolean enabled,
            boolean locked,
            boolean emailVerified,
            UserLocaleSupportedLanguages locale,
            List<Role> roles,
            LocalCredential credential) {
        return withId(new User(
                email,
                enabled,
                locked,
                emailVerified,
                null,
                FIXED_NOW,
                FIXED_NOW,
                locale,
                roles,
                credential,
                new ArrayList<>()
        ), id);
    }

    public static AuthIdentity authIdentity(Long id, User user) {
        return authIdentity(id, user, GOOGLE_UID);
    }

    public static AuthIdentity authIdentity(Long id, User user, String uid) {
        return withId(new AuthIdentity(
                user,
                AuthIdentityProvider.GOOGLE,
                uid,
                GOOGLE_PICTURE,
                FIXED_NOW,
                FIXED_NOW
        ), id);
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
