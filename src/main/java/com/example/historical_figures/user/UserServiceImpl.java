package com.example.historical_figures.user;

import com.example.historical_figures.role.Role;
import com.example.historical_figures.role.RoleErrorCodes;
import com.example.historical_figures.role.RoleRepository;
import com.example.historical_figures.shared.exceptions.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String USER_ROLE = "ROLE_USER";
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new DomainException(UserErrorCodes.USER_NOT_FOUND)
                );

        return UserMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new DomainException(UserErrorCodes.USER_NOT_FOUND)
                );

        return UserMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse save(UserCreateRequest userCreateRequest) {
        User user = new User();
        user.setEmail(userCreateRequest.email().toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(userCreateRequest.password()));
        user.setPasswordChangedAt(Instant.now());
        user.setIsEnabled(true);
        user.setIsLocked(false);

        Role userRole = roleRepository.findByName(USER_ROLE)
                .orElseThrow(() -> new DomainException(RoleErrorCodes.ROLE_NOT_FOUND, "Role not found"));

        user.setRoles(new ArrayList<>(List.of(userRole)));

        try{
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            String msg = String.valueOf(e.getMostSpecificCause().getMessage());
            if (msg.contains("uq_users_email")) {
                throw new DomainException(UserErrorCodes.USER_ALREADY_EXISTS);
            }
            
            throw e;
        }


        return UserMapper.toResponse(user);
    }
}
