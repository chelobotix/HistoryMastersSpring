package com.example.historical_figures.biography;

import com.example.historical_figures.figure.Figure;
import com.example.historical_figures.shared.models.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "biographies")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Biography extends BaseEntity {

    // Relations
    @Setter
    @OneToOne
    @JoinColumn(name = "figure_id")
    private Figure figure;

    @OneToMany(mappedBy = "biography")
    private List<BiographyTranslation> translations = new ArrayList<>();
}
