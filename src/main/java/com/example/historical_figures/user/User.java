package com.example.historical_figures.user;

import com.example.historical_figures.auth_identity.AuthIdentity;
import com.example.historical_figures.local_credential.LocalCredential;
import com.example.historical_figures.role.Role;
import com.example.historical_figures.shared.models.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @Setter
    @Column(name = "email")
    private String email;

    @Setter
    @Column(name = "is_enabled")
    private Boolean isEnabled = false;

    @Setter
    @Column(name = "is_locked")
    private Boolean isLocked = false;

    @Setter
    @Column(name = "is_email_verified")
    private Boolean isEmailVerified = false;

    @Setter
    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "locale", nullable = false)
    private UserLocaleSupportedLanguages locale;

    // Relations
    @Setter
    @ManyToMany
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"),
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "role_id"})
    )
    private List<Role> roles;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, optional = true, fetch = FetchType.EAGER)
    private LocalCredential localCredential;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AuthIdentity> authIdentities;
}
