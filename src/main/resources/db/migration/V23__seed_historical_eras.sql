-- Seed: classic historical ages with EN/ES/PT names.
-- code follows the business-key convention: leading underscore + lowercase.
-- sort_order keeps the chronological order independent of the generated id.
-- Locale values match the UserLocaleSupportedLanguages enum (EN, ES, PT).

INSERT INTO historical_eras (code, sort_order)
VALUES
    ('_prehistory', 1),
    ('_ancient_age', 2),
    ('_middle_ages', 3),
    ('_modern_age', 4),
    ('_contemporary_age', 5);

INSERT INTO historical_era_translations (historical_era_id, locale, text)
SELECT e.id, t.locale, t.text
FROM historical_eras e
JOIN (
    VALUES
        ('_prehistory', 'EN', 'Prehistory'),
        ('_prehistory', 'ES', 'Prehistoria'),
        ('_prehistory', 'PT', 'Pré-História'),
        ('_ancient_age', 'EN', 'Ancient Age'),
        ('_ancient_age', 'ES', 'Edad Antigua'),
        ('_ancient_age', 'PT', 'Idade Antiga'),
        ('_middle_ages', 'EN', 'Middle Ages'),
        ('_middle_ages', 'ES', 'Edad Media'),
        ('_middle_ages', 'PT', 'Idade Média'),
        ('_modern_age', 'EN', 'Modern Age'),
        ('_modern_age', 'ES', 'Edad Moderna'),
        ('_modern_age', 'PT', 'Idade Moderna'),
        ('_contemporary_age', 'EN', 'Contemporary Age'),
        ('_contemporary_age', 'ES', 'Edad Contemporánea'),
        ('_contemporary_age', 'PT', 'Idade Contemporânea')
    ) AS t (code, locale, text) ON e.code = t.code;
