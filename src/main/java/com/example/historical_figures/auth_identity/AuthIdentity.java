package com.example.historical_figures.auth_identity;

import com.example.historical_figures.shared.models.BaseEntity;
import com.example.historical_figures.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "auth_identities")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthIdentity extends BaseEntity {

    @Setter
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private AuthIdentityProvider provider;

    @Setter
    @Column(name = "uid", nullable = false)
    private String uid;

    @Setter
    @Column(name = "picture")
    private String picture;
    
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;

}
