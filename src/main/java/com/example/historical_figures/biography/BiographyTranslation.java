package com.example.historical_figures.biography;

import com.example.historical_figures.shared.models.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "biography_translations")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BiographyTranslation extends BaseEntity {

    @Setter
    @Column(name = "locale")
    private String locale;

    @Setter
    @Column(name = "text")
    private String text;

    // Relations
    @ManyToOne
    @JoinColumn(name = "biography_id")
    private Biography biography;
}
