package com.example.historical_figures.security.config;

import com.example.historical_figures.shared.exceptions.DomainException;
import com.example.historical_figures.user.User;
import com.example.historical_figures.user.UserErrorCodes;
import com.example.historical_figures.user.UserLocaleSupportedLanguages;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Locale;
import java.util.Optional;

@Component
public class AuthenticationFacade {

    // Where an anonymous visitor's chosen locale lives in the HTTP session. Kept here so
    // both the LocaleResolver (write + read) and this facade read the same attribute.
    public static final String ANONYMOUS_LOCALE_SESSION_KEY = "ANONYMOUS_LOCALE";

    public boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);
    }

    public User getCurrentUser() {
        if (!isAuthenticated()) throw new DomainException(UserErrorCodes.USER_NOT_LOGGED_IN);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();

        return details.getUser();
    }

    public UserLocaleSupportedLanguages getLocaleFromCurrentUser() {
        return getCurrentUser().getLocale();
    }

    /**
     * The locale an anonymous visitor chose during this session, if any was stored.
     */
    public Optional<Locale> getLocaleFromSession() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpSession session = attributes.getRequest().getSession(false);
            if (session != null && session.getAttribute(ANONYMOUS_LOCALE_SESSION_KEY) instanceof Locale locale) {
                return Optional.of(locale);
            }
        }

        return Optional.empty();
    }
}
