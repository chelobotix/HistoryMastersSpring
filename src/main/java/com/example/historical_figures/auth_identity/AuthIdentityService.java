package com.example.historical_figures.auth_identity;

import org.springframework.security.oauth2.core.user.OAuth2User;

public interface AuthIdentityService {
    void oauth2Factory(OAuth2User oauth2User, AuthIdentityProvider provider);
}
