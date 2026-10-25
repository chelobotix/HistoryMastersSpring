package com.example.historical_figures.historical_era;

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

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "historical_eras")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalEra extends BaseEntity {

    @Setter
    @Column(name = "code")
    private String code;

    @Setter
    @Column(name = "sort_order")
    private Integer sortOrder;

    // Relations
    @OneToMany(mappedBy = "historicalEra")
    private List<Figure> figures = new ArrayList<>();

    @OneToMany(mappedBy = "historicalEra")
    private List<HistoricalEraTranslation> translations = new ArrayList<>();
}
