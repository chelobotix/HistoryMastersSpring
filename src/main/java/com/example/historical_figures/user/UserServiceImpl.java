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
}
