package com.example.historical_figures.role;

import com.example.historical_figures.user.User;

import java.util.Collection;

public interface RoleService {
    void addRoles(User user, Collection<RoleName> newRoles);
}
