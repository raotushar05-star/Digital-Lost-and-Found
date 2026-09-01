-- =====================================================================
-- Digital Lost-and-Found Network — Initial Schema (frozen, 18 tables)
-- Source: 03_Database_Specification.docx
-- =====================================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ---------------------------------------------------------------------
-- 4. locations  (created first: referenced by police_stations, items)
-- ---------------------------------------------------------------------
CREATE TABLE locations (
    location_id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    latitude      DECIMAL(9,6) NOT NULL,
    longitude     DECIMAL(9,6) NOT NULL,
    address_text  TEXT,
    locality      VARCHAR(100),
    city          VARCHAR(100) NOT NULL,
    state         VARCHAR(100),
    postal_code   VARCHAR(10),
    created_at    TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_locations_city ON locations(city);

-- ---------------------------------------------------------------------
-- 3. categories
-- ---------------------------------------------------------------------
CREATE TABLE categories (
    category_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_name   VARCHAR(80) NOT NULL UNIQUE,
    description     TEXT,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
-- 2. police_stations
-- ---------------------------------------------------------------------
CREATE TABLE police_stations (
    station_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    station_name   VARCHAR(150) NOT NULL,
    station_code   VARCHAR(30) NOT NULL UNIQUE,
    address        TEXT NOT NULL,
    phone          VARCHAR(15),
    location_id    UUID REFERENCES locations(location_id),
    is_active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
-- 1. users
-- ---------------------------------------------------------------------
CREATE TABLE users (
    user_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name           VARCHAR(100) NOT NULL,
    email          VARCHAR(150) UNIQUE,
    phone          VARCHAR(15) NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL,
    role           VARCHAR(30) NOT NULL,
    station_id     UUID REFERENCES police_stations(station_id),
    address        TEXT,
    is_active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_users_role CHECK (role IN ('USER','POLICE_OFFICER','POLICE_ADMIN','SYSTEM_ADMIN'))
);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_station ON users(station_id);

-- ---------------------------------------------------------------------
-- 5. lost_items
-- ---------------------------------------------------------------------
CREATE TABLE lost_items (
    lost_item_id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id              UUID NOT NULL REFERENCES users(user_id),
    category_id           UUID NOT NULL REFERENCES categories(category_id),
    description           TEXT NOT NULL,
    brand                 VARCHAR(100),
    color                 VARCHAR(50),
    identifying_details   TEXT,
    lost_date             DATE NOT NULL,
    location_id           UUID NOT NULL REFERENCES locations(location_id),
    status                VARCHAR(30) NOT NULL DEFAULT 'REPORTED',
    created_at            TIMESTAMP NOT NULL DEFAULT now(),
    updated_at            TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_lost_items_owner ON lost_items(owner_id);
CREATE INDEX idx_lost_items_category ON lost_items(category_id);
CREATE INDEX idx_lost_items_status ON lost_items(status);

-- ---------------------------------------------------------------------
-- 6. found_reports  (preliminary finder reports)
-- ---------------------------------------------------------------------
CREATE TABLE found_reports (
    found_report_id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    finder_id         UUID NOT NULL REFERENCES users(user_id),
    category_id       UUID NOT NULL REFERENCES categories(category_id),
    description       TEXT NOT NULL,
    brand             VARCHAR(100),
    color             VARCHAR(50),
    found_date        DATE NOT NULL,
    location_id       UUID NOT NULL REFERENCES locations(location_id),
    status            VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED',
    created_at        TIMESTAMP NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_found_reports_status CHECK (status IN ('SUBMITTED','RECEIVED','LINKED','REJECTED','DUPLICATE'))
);
CREATE INDEX idx_found_reports_finder ON found_reports(finder_id);
CREATE INDEX idx_found_reports_status ON found_reports(status);

-- ---------------------------------------------------------------------
-- 7. found_items  (official, police-custody items)
-- ---------------------------------------------------------------------
CREATE TABLE found_items (
    found_item_id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    found_report_id               UUID UNIQUE REFERENCES found_reports(found_report_id),
    station_id                    UUID NOT NULL REFERENCES police_stations(station_id),
    category_id                   UUID NOT NULL REFERENCES categories(category_id),
    description                   TEXT NOT NULL,
    brand                         VARCHAR(100),
    color                         VARCHAR(50),
    private_identifying_details   TEXT,
    found_date                    DATE NOT NULL,
    received_date                 TIMESTAMP NOT NULL DEFAULT now(),
    location_id                   UUID NOT NULL REFERENCES locations(location_id),
    custody_status                VARCHAR(30) NOT NULL DEFAULT 'IN_CUSTODY',
    verification_status           VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at                    TIMESTAMP NOT NULL DEFAULT now(),
    updated_at                    TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_found_items_custody CHECK (custody_status IN ('IN_CUSTODY','CLAIMED','RETURNED','TRANSFERRED')),
    CONSTRAINT chk_found_items_verification CHECK (verification_status IN ('PENDING','VERIFIED','REJECTED'))
);
CREATE INDEX idx_found_items_station ON found_items(station_id);
CREATE INDEX idx_found_items_category ON found_items(category_id);
CREATE INDEX idx_found_items_verification ON found_items(verification_status);
CREATE INDEX idx_found_items_custody ON found_items(custody_status);

-- ---------------------------------------------------------------------
-- 8. item_photos
-- ---------------------------------------------------------------------
CREATE TABLE item_photos (
    photo_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lost_item_id   UUID REFERENCES lost_items(lost_item_id) ON DELETE CASCADE,
    found_item_id  UUID REFERENCES found_items(found_item_id) ON DELETE CASCADE,
    file_url       VARCHAR(500) NOT NULL,
    is_primary     BOOLEAN NOT NULL DEFAULT FALSE,
    visibility     VARCHAR(30) NOT NULL DEFAULT 'PUBLIC',
    uploaded_by    UUID NOT NULL REFERENCES users(user_id),
    created_at     TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_item_photos_owner CHECK (
        (lost_item_id IS NOT NULL AND found_item_id IS NULL) OR
        (lost_item_id IS NULL AND found_item_id IS NOT NULL)
    ),
    CONSTRAINT chk_item_photos_visibility CHECK (visibility IN ('PUBLIC','PRIVATE','RESTRICTED'))
);
CREATE INDEX idx_item_photos_lost ON item_photos(lost_item_id);
CREATE INDEX idx_item_photos_found ON item_photos(found_item_id);

-- ---------------------------------------------------------------------
-- 9. cases
-- ---------------------------------------------------------------------
CREATE TABLE cases (
    case_id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_number      VARCHAR(30) NOT NULL UNIQUE,
    lost_item_id     UUID UNIQUE REFERENCES lost_items(lost_item_id),
    found_item_id    UUID UNIQUE REFERENCES found_items(found_item_id),
    case_type        VARCHAR(20) NOT NULL,
    current_status   VARCHAR(40) NOT NULL DEFAULT 'REPORTED',
    created_at       TIMESTAMP NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_cases_type CHECK (case_type IN ('LOST','FOUND'))
);
CREATE INDEX idx_cases_status ON cases(current_status);

-- ---------------------------------------------------------------------
-- 10. matches
-- ---------------------------------------------------------------------
CREATE TABLE matches (
    match_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lost_item_id    UUID NOT NULL REFERENCES lost_items(lost_item_id),
    found_item_id   UUID NOT NULL REFERENCES found_items(found_item_id),
    match_score     DECIMAL(5,2) NOT NULL,
    match_reason    TEXT,
    status          VARCHAR(30) NOT NULL DEFAULT 'GENERATED',
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_matches_status CHECK (status IN ('GENERATED','NOTIFIED','CLAIMED','DISMISSED','CONFIRMED')),
    CONSTRAINT uq_matches_pair UNIQUE (lost_item_id, found_item_id)
);
CREATE INDEX idx_matches_lost ON matches(lost_item_id);
CREATE INDEX idx_matches_found ON matches(found_item_id);

-- ---------------------------------------------------------------------
-- 11. claims
-- ---------------------------------------------------------------------
CREATE TABLE claims (
    claim_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    found_item_id   UUID NOT NULL REFERENCES found_items(found_item_id),
    claimant_id     UUID NOT NULL REFERENCES users(user_id),
    lost_item_id    UUID REFERENCES lost_items(lost_item_id),
    claim_details   TEXT NOT NULL,
    status          VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    reviewed_by     UUID REFERENCES users(user_id),
    reviewed_at     TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_claims_status CHECK (status IN ('PENDING','UNDER_VERIFICATION','APPROVED','REJECTED','DISPUTED'))
);
CREATE INDEX idx_claims_found_item ON claims(found_item_id);
CREATE INDEX idx_claims_claimant ON claims(claimant_id);
CREATE INDEX idx_claims_status ON claims(status);

-- ---------------------------------------------------------------------
-- 12. claim_evidence
-- ---------------------------------------------------------------------
CREATE TABLE claim_evidence (
    evidence_id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    claim_id               UUID NOT NULL REFERENCES claims(claim_id) ON DELETE CASCADE,
    evidence_type          VARCHAR(40) NOT NULL,
    description            TEXT,
    file_url               VARCHAR(500),
    verification_status    VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    verified_by             UUID REFERENCES users(user_id),
    verified_at             TIMESTAMP,
    created_at              TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_evidence_verification CHECK (verification_status IN ('PENDING','ACCEPTED','REJECTED','INCONCLUSIVE'))
);
CREATE INDEX idx_evidence_claim ON claim_evidence(claim_id);

-- ---------------------------------------------------------------------
-- 13. verification_records
-- ---------------------------------------------------------------------
CREATE TABLE verification_records (
    verification_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    found_item_id         UUID REFERENCES found_items(found_item_id),
    claim_id              UUID REFERENCES claims(claim_id),
    officer_id            UUID NOT NULL REFERENCES users(user_id),
    verification_type     VARCHAR(40) NOT NULL,
    decision               VARCHAR(30) NOT NULL,
    verification_notes    TEXT,
    verified_at            TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_verification_type CHECK (verification_type IN ('FOUND_ITEM_VERIFICATION','OWNERSHIP_VERIFICATION'))
);
CREATE INDEX idx_verification_found_item ON verification_records(found_item_id);
CREATE INDEX idx_verification_claim ON verification_records(claim_id);

-- ---------------------------------------------------------------------
-- 14. claim_disputes
-- ---------------------------------------------------------------------
CREATE TABLE claim_disputes (
    dispute_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    found_item_id   UUID NOT NULL REFERENCES found_items(found_item_id),
    claim_id        UUID NOT NULL REFERENCES claims(claim_id),
    raised_by       UUID REFERENCES users(user_id),
    reason          TEXT NOT NULL,
    status          VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    resolution      TEXT,
    resolved_by     UUID REFERENCES users(user_id),
    resolved_at     TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_dispute_status CHECK (status IN ('OPEN','UNDER_REVIEW','RESOLVED','CLOSED'))
);
CREATE INDEX idx_disputes_found_item ON claim_disputes(found_item_id);
CREATE INDEX idx_disputes_claim ON claim_disputes(claim_id);

-- ---------------------------------------------------------------------
-- 15. notifications
-- ---------------------------------------------------------------------
CREATE TABLE notifications (
    notification_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id              UUID NOT NULL REFERENCES users(user_id),
    notification_type    VARCHAR(40) NOT NULL,
    title                VARCHAR(150) NOT NULL,
    message              TEXT NOT NULL,
    related_case_id      UUID REFERENCES cases(case_id),
    related_match_id     UUID REFERENCES matches(match_id),
    is_read              BOOLEAN NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_read ON notifications(user_id, is_read);

-- ---------------------------------------------------------------------
-- 16. handover_records
-- ---------------------------------------------------------------------
CREATE TABLE handover_records (
    handover_id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    found_item_id                UUID NOT NULL UNIQUE REFERENCES found_items(found_item_id),
    claim_id                     UUID NOT NULL REFERENCES claims(claim_id),
    recipient_id                 UUID NOT NULL REFERENCES users(user_id),
    officer_id                   UUID NOT NULL REFERENCES users(user_id),
    handover_date                TIMESTAMP NOT NULL DEFAULT now(),
    handover_notes               TEXT,
    acknowledgement_reference    VARCHAR(255),
    created_at                   TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_handover_claim ON handover_records(claim_id);

-- ---------------------------------------------------------------------
-- 17. case_status_history
-- ---------------------------------------------------------------------
CREATE TABLE case_status_history (
    status_history_id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id              UUID NOT NULL REFERENCES cases(case_id) ON DELETE CASCADE,
    old_status           VARCHAR(40),
    new_status           VARCHAR(40) NOT NULL,
    changed_by           UUID NOT NULL REFERENCES users(user_id),
    remarks              TEXT,
    changed_at           TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_status_history_case ON case_status_history(case_id);

-- ---------------------------------------------------------------------
-- 18. audit_logs  (append-only)
-- ---------------------------------------------------------------------
CREATE TABLE audit_logs (
    audit_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id        UUID REFERENCES users(user_id),
    action         VARCHAR(100) NOT NULL,
    entity_type    VARCHAR(50) NOT NULL,
    entity_id      UUID,
    old_value      JSONB,
    new_value      JSONB,
    ip_address     VARCHAR(45),
    created_at     TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
