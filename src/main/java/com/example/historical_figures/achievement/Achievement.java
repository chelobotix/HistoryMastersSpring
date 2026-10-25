package com.example.historical_figures.achievement;

import com.example.historical_figures.figure.Figure;
import com.example.historical_figures.shared.models.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "achievements")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Achievement extends BaseEntity {

    @Setter
    @Column(name = "code")
    private String code;

    // Relations
    @ManyToMany(mappedBy = "achievements")
    private Set<Figure> figures = new HashSet<>();

    @OneToMany(mappedBy = "achievement")
    private List<AchievementTranslation> translations = new ArrayList<>();
}
