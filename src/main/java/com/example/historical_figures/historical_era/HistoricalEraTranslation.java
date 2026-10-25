package com.example.historical_figures.historical_era;

import com.example.historical_figures.shared.models.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "historical_era_translations")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalEraTranslation extends BaseEntity {

    @Setter
    @Column(name = "locale")
    private String locale;

    @Setter
    @Column(name = "text")
    private String text;

    // Relations
    @ManyToOne
    @JoinColumn(name = "historical_era_id")
    private HistoricalEra historicalEra;
}
