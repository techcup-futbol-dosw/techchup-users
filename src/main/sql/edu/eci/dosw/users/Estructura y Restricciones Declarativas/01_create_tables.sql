-- =============================================================
-- TECHCUP FÚTBOL - Servicio de Usuarios
-- Estructura y Restricciones Declarativas
-- =============================================================

-- -------------------------------------------------------------
-- TABLA: user_profiles
-- -------------------------------------------------------------
CREATE TABLE user_profiles (
    id                  BIGSERIAL       PRIMARY KEY,
    full_name           VARCHAR(150)    NOT NULL,
    email               VARCHAR(255)    NOT NULL,
    password            VARCHAR(255)    NOT NULL,
    identification      VARCHAR(20)     NOT NULL,
    birth_date          DATE,
    gender              VARCHAR(10),
    school_relation     VARCHAR(20),
    academic_program    VARCHAR(100),
    semester            INTEGER,
    status              VARCHAR(10)     NOT NULL    DEFAULT 'ACTIVE',
    profile_created_at  TIMESTAMP,
    updated_at          TIMESTAMP,

    CONSTRAINT uq_user_profiles_email
        UNIQUE (email),

    CONSTRAINT uq_user_profiles_identification
        UNIQUE (identification),

    CONSTRAINT chk_user_profiles_status
        CHECK (status IN ('ACTIVE', 'INACTIVE')),

    CONSTRAINT chk_user_profiles_gender
        CHECK (gender IS NULL OR gender IN ('MALE', 'FEMALE', 'OTHER')),

    CONSTRAINT chk_user_profiles_school_relation
        CHECK (school_relation IS NULL OR school_relation IN (
            'STUDENT', 'PROFESSOR', 'ADMINISTRATIVE', 'GRADUATE', 'FAMILY'
        )),

    CONSTRAINT chk_user_profiles_semester
        CHECK (semester IS NULL OR (semester >= 1 AND semester <= 10)),

    CONSTRAINT chk_user_profiles_birth_date
        CHECK (birth_date IS NULL OR birth_date < CURRENT_DATE)
);


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

    CONSTRAINT fk_sport_profiles_user
        FOREIGN KEY (user_id)
        REFERENCES user_profiles (id),

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

    CONSTRAINT fk_invitations_player
        FOREIGN KEY (player_id)
        REFERENCES user_profiles (id),

    CONSTRAINT chk_invitations_status
        CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'CANCELLED')),

    CONSTRAINT chk_invitations_responded_at
        CHECK (responded_at IS NULL OR responded_at >= sent_at)
);
