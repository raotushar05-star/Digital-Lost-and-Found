-- Handover OTP
ALTER TABLE claims
    ADD COLUMN handover_otp_hash VARCHAR(100),
    ADD COLUMN handover_otp_expires_at TIMESTAMP,
    ADD COLUMN handover_otp_attempts INT NOT NULL DEFAULT 0;

-- Station transfers
CREATE TABLE custody_transfers (
    transfer_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    found_item_id    UUID NOT NULL REFERENCES found_items(found_item_id),
    from_station_id  UUID NOT NULL REFERENCES police_stations(station_id),
    to_station_id    UUID NOT NULL REFERENCES police_stations(station_id),
    transferred_by   UUID NOT NULL REFERENCES users(user_id),
    reason           TEXT NOT NULL,
    transferred_at   TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_custody_transfers_item ON custody_transfers(found_item_id);

-- Disposal
ALTER TABLE found_items DROP CONSTRAINT chk_found_items_custody;
ALTER TABLE found_items ADD CONSTRAINT chk_found_items_custody
    CHECK (custody_status IN ('IN_CUSTODY','CLAIMED','RETURNED','TRANSFERRED','DISPOSED'));
ALTER TABLE found_items
    ADD COLUMN disposed_at TIMESTAMP,
    ADD COLUMN disposal_method VARCHAR(40),
    ADD COLUMN disposal_notes TEXT;