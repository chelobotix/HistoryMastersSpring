package com.example.historical_figures.figure;

import com.example.historical_figures.achievement.Achievement;
import com.example.historical_figures.area.Area;
import com.example.historical_figures.biography.Biography;
import com.example.historical_figures.country.Country;
import com.example.historical_figures.historical_era.HistoricalEra;
import com.example.historical_figures.profession.Profession;
import com.example.historical_figures.shared.models.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "figures")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Figure extends BaseEntity {

    @Setter
    @Column(name = "first_name")
    private String firstName;

    @Setter
    @Column(name = "last_name")
    private String lastName;

    @Setter
    @Column(name = "year_of_birth")
    private Integer yearOfBirth;

    @Setter
    @Column(name = "year_of_birth_bc")
    private Boolean yearOfBirthBC = false;

    @Setter
    @Column(name = "year_of_death")
    private Integer yearOfDeath;

    @Setter
    @Column(name = "year_of_death_bc")
    private Boolean yearOfDeathBC = false;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;

    // Relations
    @OneToOne(mappedBy = "figure")
    private Biography biography;

    @ManyToOne
    @JoinColumn(name = "country_id")
    private Country country;

    @ManyToOne
    @JoinColumn(name = "historical_era_id")
    private HistoricalEra historicalEra;

    @ManyToMany
    @JoinTable(
            name = "figure_professions",
            joinColumns = @JoinColumn(name = "figure_id"),
            inverseJoinColumns = @JoinColumn(name = "profession_id")
    )
    private Set<Profession> professions = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "figure_areas",
            joinColumns = @JoinColumn(name = "figure_id"),
            inverseJoinColumns = @JoinColumn(name = "area_id")
    )
    private Set<Area> areas = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "figure_achievements",
            joinColumns = @JoinColumn(name = "figure_id"),
            inverseJoinColumns = @JoinColumn(name = "achievement_id")
    )
    private Set<Achievement> achievements = new HashSet<>();

}
