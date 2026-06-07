package com.example.historical_figures.user;

import com.example.historical_figures.role.Role;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

public class UserMapper {

    public static UserResponse toResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getRoles().stream().map(Role::getName).collect(Collectors.toList())
        );
    }
}
