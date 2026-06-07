package com.example.historical_figures.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Ignores background requests (e.g. Chrome DevTools workspace discovery) so they are
 * not used as the post-login redirect target.
 */
@Component
public class FilteredRequestCache extends HttpSessionRequestCache {

    private static final List<String> IGNORED_PATHS = List.of(
            "/.well-known/**",
            "/favicon.ico",
            "/error/**"
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public void saveRequest(HttpServletRequest request, HttpServletResponse response) {
        if (shouldIgnore(request.getRequestURI())) {
            return;
        }
        super.saveRequest(request, response);
    }

    @Override
    public SavedRequest getRequest(HttpServletRequest request, HttpServletResponse response) {
        SavedRequest savedRequest = super.getRequest(request, response);
        if (savedRequest == null) {
            return null;
        }

        try {
            String path = new URI(savedRequest.getRedirectUrl()).getPath();
            if (shouldIgnore(path)) {
                removeRequest(request, response);
                return null;
            }
        } catch (URISyntaxException ignored) {
            removeRequest(request, response);
            return null;
        }

        return savedRequest;
    }

    private boolean shouldIgnore(String path) {
        return IGNORED_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
}
