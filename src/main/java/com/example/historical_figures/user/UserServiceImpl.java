package com.example.historical_figures.user;

import com.example.historical_figures.shared.exceptions.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        User user = userRepository
                .findById(id)
                .orElseThrow(() ->
                        new DomainException(UserErrorCodes.USER_NOT_FOUND)
                );

        return UserMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getByEmail(String email) {
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new DomainException(UserErrorCodes.USER_NOT_FOUND)
                );

        return UserMapper.toResponse(user);
    }

    @Override
    public void setLocale(User user, String locale) {
        try {
            UserLocaleSupportedLanguages lang = UserLocaleSupportedLanguages.valueOf(locale.toUpperCase());
            user.setLocale(lang);
            userRepository.save(user);
        } catch (IllegalArgumentException e) {
            throw new DomainException(UserErrorCodes.USER_INVALID_LOCALE);
        }
    }
}
