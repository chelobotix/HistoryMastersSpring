package com.example.historical_figures.role;

import com.example.historical_figures.shared.exceptions.DomainException;
import com.example.historical_figures.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public void addRoles(User user, Collection<RoleName> newRoles) {
        List<String> names = newRoles.stream().map(Enum::name).distinct().toList();
        List<Role> found = roleRepository.findByNameIn(names);

        if (found.size() != names.size()) {
            throw new DomainException(RoleErrorCodes.ROLE_NOT_FOUND);
        }

        if (user.getRoles() == null) {
            user.setRoles(new ArrayList<>(found));
            return;
        }

        found.stream().filter(r -> !user.getRoles().contains(r)).forEach(user.getRoles()::add);
    }
}
