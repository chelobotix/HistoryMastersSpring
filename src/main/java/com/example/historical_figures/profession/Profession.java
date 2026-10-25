package com.example.historical_figures.profession;

import com.example.historical_figures.figure.Figure;
import com.example.historical_figures.shared.models.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "professions")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Profession extends BaseEntity {

    @Setter
    @Column(name = "code")
    private String code;

    // Relations
    @ManyToMany(mappedBy = "professions")
    private Set<Figure> figures = new HashSet<>();

    @OneToMany(mappedBy = "profession")
    private List<ProfessionTranslation> translations = new ArrayList<>();
}
