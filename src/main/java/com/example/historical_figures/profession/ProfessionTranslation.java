package com.example.historical_figures.profession;

import com.example.historical_figures.shared.models.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "profession_translations")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionTranslation extends BaseEntity {

    @Setter
    @Column(name = "locale")
    private String locale;

    @Setter
    @Column(name = "text")
    private String text;

    // Relations
    @ManyToOne
    @JoinColumn(name = "profession_id")
    private Profession profession;
}
