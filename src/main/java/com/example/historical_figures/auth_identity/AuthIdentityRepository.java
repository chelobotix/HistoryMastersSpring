package com.example.historical_figures.auth_identity;

import com.example.historical_figures.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AuthIdentityRepository extends JpaRepository<AuthIdentity, Long> {

    public Optional<AuthIdentity> findByProviderAndUid(AuthIdentityProvider provider, String uid);
    public Optional<AuthIdentity> findByUserAndProvider(User user, AuthIdentityProvider provider);
}
