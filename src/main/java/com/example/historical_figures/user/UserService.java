package com.example.historical_figures.user;

public interface UserService {

    UserResponse getById(Long id);
    UserResponse getByEmail(String email);
    UserResponse save(UserCreateRequest userCreateRequest);
}
