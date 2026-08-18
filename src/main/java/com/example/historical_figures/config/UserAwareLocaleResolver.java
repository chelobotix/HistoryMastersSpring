package com.example.historical_figures.config;

import com.example.historical_figures.security.config.AuthenticationFacade;
import com.example.historical_figures.user.UserLocaleSupportedLanguages;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

@Component("localeResolver")
@RequiredArgsConstructor
public class UserAwareLocaleResolver implements LocaleResolver {

    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    private final AuthenticationFacade authFacade;

    @Override
    @NonNull
    public Locale resolveLocale(@NonNull HttpServletRequest request) {
        // 1) Authenticated: source of truth is the DB, already carried in the principal
        if (authFacade.isAuthenticated()) {
            return toLocale(authFacade.getCurrentUser().getLocale());
        }

        // 2) Anonymous: whatever they chose in this session or default
        return authFacade.getLocaleFromSession()
            .orElse(DEFAULT_LOCALE);
    }

    @Override
    public void setLocale(@NonNull HttpServletRequest request,
                          @Nullable HttpServletResponse response,
                          @Nullable Locale locale) {
        // Anonymous users only.
        request.getSession(true).setAttribute(AuthenticationFacade.ANONYMOUS_LOCALE_SESSION_KEY, locale);
    }

    private Locale toLocale(UserLocaleSupportedLanguages lang) {
        return Locale.of(lang.name().toLowerCase());
    }
}
