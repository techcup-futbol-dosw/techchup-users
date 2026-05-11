-- =============================================================
-- TECHCUP FÚTBOL - Servicio de Usuarios
-- Estructura y Restricciones Declarativas
-- =============================================================
-- NOTA: La tabla 'users' fue eliminada de este microservicio.
-- La gestión de usuarios es responsabilidad del servicio de
-- identidad externo. Los campos user_id / player_id son
-- referencias lógicas al ID del usuario en dicho servicio.
-- =============================================================


-- -------------------------------------------------------------
-- TABLA: sport_profiles
-- -------------------------------------------------------------
CREATE TABLE sport_profiles (
    id              BIGSERIAL       PRIMARY KEY,
    position        VARCHAR(15)     NOT NULL,
    dorsal_number   INTEGER,
    photo_id        VARCHAR(24),
    available       BOOLEAN         NOT NULL    DEFAULT TRUE,
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP,
    user_id         BIGINT          NOT NULL,

    CONSTRAINT uq_sport_profiles_user
        UNIQUE (user_id),

    CONSTRAINT chk_sport_profiles_position
        CHECK (position IN ('GOALKEEPER', 'DEFENDER', 'MIDFIELDER', 'FORWARD')),

    CONSTRAINT chk_sport_profiles_dorsal
        CHECK (dorsal_number IS NULL OR (dorsal_number >= 1 AND dorsal_number <= 99))
);


-- -------------------------------------------------------------
-- TABLA: invitations
-- -------------------------------------------------------------
CREATE TABLE invitations (
    id              BIGSERIAL       PRIMARY KEY,
    team_id         BIGINT          NOT NULL,
    status          VARCHAR(15)     NOT NULL    DEFAULT 'PENDING',
    sent_at         TIMESTAMP,
    responded_at    TIMESTAMP,
    player_id       BIGINT          NOT NULL,

    CONSTRAINT chk_invitations_status
        CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'CANCELLED')),

    CONSTRAINT chk_invitations_responded_at
        CHECK (responded_at IS NULL OR responded_at >= sent_at)
);


-- -------------------------------------------------------------
-- TABLA: audit_logs
-- -------------------------------------------------------------
CREATE TABLE audit_logs (
    id                  BIGSERIAL       PRIMARY KEY,
    action              VARCHAR(15)     NOT NULL,
    action_timestamp    TIMESTAMP       NOT NULL,
    details             TEXT,
    sport_profile_id    BIGINT,
    invitation_id       BIGINT,

    CONSTRAINT fk_audit_logs_sport_profile
        FOREIGN KEY (sport_profile_id)
        REFERENCES sport_profiles (id),

    CONSTRAINT fk_audit_logs_invitation
        FOREIGN KEY (invitation_id)
        REFERENCES invitations (id),

    CONSTRAINT chk_audit_logs_action
        CHECK (action IN ('CREATE', 'UPDATE', 'DEACTIVATE')),

    CONSTRAINT chk_audit_log_has_reference
        CHECK (sport_profile_id IS NOT NULL OR invitation_id IS NOT NULL)
);
