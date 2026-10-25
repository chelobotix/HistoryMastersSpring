package com.example.historical_figures.country;

import com.example.historical_figures.figure.Figure;
import com.example.historical_figures.shared.models.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "countries")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Country extends BaseEntity {

    @Setter
    @Column(name = "code")
    private String code;

    @Setter
    @Column(name = "is_iso")
    private Boolean isIso = true;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;

    // Relations
    @OneToMany(mappedBy = "country")
    private List<Figure> figures = new ArrayList<>();

    @OneToMany(mappedBy = "country")
    private List<CountryTranslation> translations = new ArrayList<>();

}
