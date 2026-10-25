package com.example.historical_figures.local_credential;

import com.example.historical_figures.shared.models.BaseEntity;
import com.example.historical_figures.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "local_credentials")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LocalCredential extends BaseEntity {

    @Setter
    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Setter
    @Column(name = "password_digest", nullable = false)
    private String passwordDigest;

    @Setter
    @Column(name = "picture")
    private String picture;
    
    @Setter
    @Column(name = "password_changed_at")
    private Instant passwordChangedAt;
    
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;

}
