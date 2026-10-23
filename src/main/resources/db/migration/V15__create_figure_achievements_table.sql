CREATE TABLE figure_achievements (
    figure_id       BIGINT NOT NULL,
    achievement_id  BIGINT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_figure_achievements PRIMARY KEY (figure_id, achievement_id),
    CONSTRAINT fk_figure_achievements_figure FOREIGN KEY (figure_id) REFERENCES figures (id) ON DELETE CASCADE,
    CONSTRAINT fk_figure_achievements_achievement FOREIGN KEY (achievement_id) REFERENCES achievements (id) ON DELETE CASCADE
);

CREATE INDEX idx_figure_achievements_achievement_id ON figure_achievements (achievement_id);

CREATE TRIGGER trg_figure_achievements_updated_at
    BEFORE UPDATE ON figure_achievements
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();
