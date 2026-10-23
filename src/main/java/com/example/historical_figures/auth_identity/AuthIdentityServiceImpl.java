package com.example.historical_figures.auth_identity;

import com.example.historical_figures.role.RoleName;
import com.example.historical_figures.role.RoleService;
import com.example.historical_figures.security.config.AuthenticationFacade;
import com.example.historical_figures.shared.exceptions.DomainException;
import com.example.historical_figures.user.User;
import com.example.historical_figures.user.UserErrorCodes;
import com.example.historical_figures.user.UserLocaleSupportedLanguages;
import com.example.historical_figures.user.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthIdentityServiceImpl implements AuthIdentityService {

    private final AuthIdentityRepository authIdentityRepository;
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final Validator validator;
    private final AuthenticationFacade authenticationFacade;

    @Override
    @Transactional
    public void oauth2Factory(OAuth2User oauth2User, AuthIdentityProvider provider) {
        if (!AuthIdentityProvider.GOOGLE.equals(provider)) {
            throw new DomainException(AuthIdentityErrorCodes.AUTH_IDENTITY_PROVIDER_NOT_ALLOWED);
        }

        AuthIdentityCreateRequest attributes = getGoogleAttributes(oauth2User);

        // A known provider+uid is a returning user: already provisioned, nothing to do.
        if (authIdentityRepository.findByProviderAndUid(attributes.provider(), attributes.uid()).isPresent()) {
            return;
        }

        if (!Boolean.TRUE.equals(attributes.emailVerified())) {
            throw new DomainException(AuthIdentityErrorCodes.AUTH_IDENTITY_EMAIL_NOT_VERIFIED);
        }

        if (userRepository.findByEmail(attributes.email()).isPresent()) {
            throw new DomainException(UserErrorCodes.USER_ALREADY_EXISTS);
        }

        newOauth2GoogleUser(attributes);
    }

    private void newOauth2GoogleUser(AuthIdentityCreateRequest attributes) {
        User user = setUserProperties(attributes.email());

        AuthIdentity authIdentity = new AuthIdentity();
        authIdentity.setUser(user);
        authIdentity.setProvider(attributes.provider());
        authIdentity.setUid(attributes.uid());
        authIdentity.setPicture(attributes.picture());

        roleService.addRoles(user, List.of(RoleName.ROLE_USER));

        try{
            userRepository.saveAndFlush(user);
            authIdentityRepository.saveAndFlush(authIdentity);
        } catch (DataIntegrityViolationException e) {
            String msg = String.valueOf(e.getMostSpecificCause().getMessage());
            if (msg.contains("uq_users_email") || msg.contains("uq_auth_identities_provider_uid")) {
                throw new DomainException(UserErrorCodes.USER_ALREADY_EXISTS);
            }

            if (msg.contains("uq_auth_identities_user_provider")) {
                throw new DomainException(AuthIdentityErrorCodes.AUTH_IDENTITY_PROVIDER_ALREADY_LINKED);
            }

            throw e;
        }
    }

    private AuthIdentityCreateRequest getGoogleAttributes(OAuth2User oauth2User) {
        String email = oauth2User.getAttribute("email");
        Object rawEmailVerified = oauth2User.getAttribute("email_verified");

        AuthIdentityCreateRequest attributes = new AuthIdentityCreateRequest(
                email == null ? null : email.toLowerCase().trim(),
                AuthIdentityProvider.GOOGLE,
                oauth2User.getAttribute("sub"),
                rawEmailVerified == null ? null : Boolean.parseBoolean(String.valueOf(rawEmailVerified)),
                oauth2User.getAttribute("picture")
        );

        Set<ConstraintViolation<AuthIdentityCreateRequest>> violations = validator.validate(attributes);
        if (!violations.isEmpty()) {
            throw new DomainException(AuthIdentityErrorCodes.AUTH_IDENTITY_INCONSISTENT_PROVIDER_DATA);
        }

        return attributes;
    }

    private User setUserProperties(String email){
        User user = new User();
        user.setEmail(email);
        user.setIsEnabled(true);
        user.setIsLocked(false);
        user.setIsEmailVerified(true);

        Locale sessionLocale = authenticationFacade.getLocaleFromSession().orElse(Locale.of("en"));
        user.setLocale(UserLocaleSupportedLanguages.validate(sessionLocale.getLanguage()));

        return user;
    }

    // TODO: Implement in logged in user setting screen
    //    private UserResponse LinkOauth2User(User user, AuthIdentityCreateRequest authIdentityCreateRequest) {
    //        AuthIdentity authIdentity = new AuthIdentity();
    //        authIdentity.setUser(user);
    //        authIdentity.setProvider(authIdentityCreateRequest.provider());
    //        authIdentity.setUid(authIdentityCreateRequest.uid());
    //        authIdentity.setPicture(authIdentityCreateRequest.picture());
    //
    //        try{
    //            authIdentityRepository.saveAndFlush(authIdentity);
    //        } catch (DataIntegrityViolationException e) {
    //            String msg = String.valueOf(e.getMostSpecificCause().getMessage());
    //            if (msg.contains("uq_auth_identities_provider_uid")) {
    //                throw new DomainException(AuthIdentityErrorCodes.AUTH_IDENTITY_ALREADY_EXISTS);
    //            }
    //            if (msg.contains("uq_auth_identities_user_provider")) {
    //                throw new DomainException(AuthIdentityErrorCodes.AUTH_IDENTITY_PROVIDER_ALREADY_LINKED);
    //            }
    //
    //            throw e;
    //        }
    //
    //        return UserMapper.toResponse(user);
    //    }
}
