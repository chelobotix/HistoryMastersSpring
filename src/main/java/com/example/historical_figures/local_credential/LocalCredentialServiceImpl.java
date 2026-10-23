package com.example.historical_figures.local_credential;

import com.example.historical_figures.role.RoleName;
import com.example.historical_figures.role.RoleService;
import com.example.historical_figures.security.config.AuthenticationFacade;
import com.example.historical_figures.shared.exceptions.DomainException;
import com.example.historical_figures.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class LocalCredentialServiceImpl implements LocalCredentialService {

    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final UserRepository userRepository;
    private final LocalCredentialRepository localCredentialRepository;
    private final AuthenticationFacade authFacade;

    @Override
    @Transactional
    public UserResponse saveLocal(LocalCredentialCreateRequest localCredentialCreateRequest) {
        User user = new User();
        LocalCredential localCredential = new LocalCredential();

        user.setEmail(localCredentialCreateRequest.email().toLowerCase().trim());
        user.setIsEnabled(true);
        user.setIsLocked(false);
        user.setLocale(resolveLocale());

        localCredential.setUser(user);
        localCredential.setPasswordDigest(passwordEncoder.encode(localCredentialCreateRequest.password()));

        roleService.addRoles(user, List.of(RoleName.ROLE_USER));

        try{
            userRepository.saveAndFlush(user);
            localCredentialRepository.saveAndFlush(localCredential);
        } catch (DataIntegrityViolationException e) {
            String msg = String.valueOf(e.getMostSpecificCause().getMessage());
            if (msg.contains("uq_users_email")) {
                throw new DomainException(UserErrorCodes.USER_ALREADY_EXISTS);
            }

            throw e;
        }


        return UserMapper.toResponse(user);
    }

    /**
     * New accounts adopt the locale chosen while browsing anonymously, defaulting to English.
     */
    private UserLocaleSupportedLanguages resolveLocale() {
        return authFacade.getLocaleFromSession()
                .map(Locale::getLanguage)
                .map(String::toUpperCase)
                .map(UserLocaleSupportedLanguages::valueOf)
                .orElse(UserLocaleSupportedLanguages.EN);
    }
}
