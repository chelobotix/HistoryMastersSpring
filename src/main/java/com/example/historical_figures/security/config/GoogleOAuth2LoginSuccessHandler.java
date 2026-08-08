package com.example.historical_figures.security.config;

import com.example.historical_figures.auth_identity.AuthIdentityProvider;
import com.example.historical_figures.auth_identity.AuthIdentityService;
import com.example.historical_figures.shared.exceptions.DomainException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class GoogleOAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthIdentityService authIdentityService;
    private final CustomUserDetailsService customUserDetailsService;
    private final SecurityContextRepository securityContextRepository;
    private final SecurityContextHolderStrategy securityContextHolderStrategy =
            SecurityContextHolder.getContextHolderStrategy();
    private final SavedRequestAwareAuthenticationSuccessHandler redirectHandler;

    public GoogleOAuth2LoginSuccessHandler(
            AuthIdentityService authIdentityService,
            CustomUserDetailsService customUserDetailsService,
            SecurityContextRepository securityContextRepository,
            RequestCache requestCache) {
        this.authIdentityService = authIdentityService;
        this.customUserDetailsService = customUserDetailsService;
        this.securityContextRepository = securityContextRepository;
        this.redirectHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        this.redirectHandler.setRequestCache(requestCache);
        this.redirectHandler.setDefaultTargetUrl("/home");
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        UserDetails userDetails;
        try {
            authIdentityService.oauth2Factory(oauth2User, AuthIdentityProvider.GOOGLE);
            userDetails = customUserDetailsService.loadUserByUsername(oauth2User.getAttribute("email"));
        } catch (DomainException ex) {
            log.warn("OAuth2 login rejected: {}", ex.getErrorCode());
            rejectOAuth2(request, response);
            return;
        } catch (UsernameNotFoundException ex) {
            log.warn("OAuth2 login rejected: provisioned account could not be loaded");
            rejectOAuth2(request, response);
            return;
        }

        if (!userDetails.isEnabled() || !userDetails.isAccountNonLocked()) {
            log.warn("OAuth2 login rejected: account disabled or locked for {}", userDetails.getUsername());
            rejectOAuth2(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken newAuth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        newAuth.setDetails(authentication.getDetails());

        // The filter already persisted the OAuth2 token before calling this handler,
        // so the swapped principal only survives if it is saved again explicitly.
        SecurityContext context = this.securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(newAuth);
        this.securityContextHolderStrategy.setContext(context);
        this.securityContextRepository.saveContext(context, request, response);

        this.redirectHandler.onAuthenticationSuccess(request, response, newAuth);
    }

    private void rejectOAuth2(HttpServletRequest request, HttpServletResponse response) throws IOException {
        this.securityContextHolderStrategy.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect("/login?error=true&type=oauth2");
    }
}
