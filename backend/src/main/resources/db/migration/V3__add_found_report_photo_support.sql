-- Adds the ability to attach photos to a preliminary found_report, not just
-- to an official found_item. This strengthens the proof-of-discovery trail
-- (timestamp + location + photo) captured at the moment a citizen reports
-- finding an item, before police intake creates the official custody record.

ALTER TABLE item_photos
    ADD COLUMN found_report_id UUID REFERENCES found_reports(found_report_id) ON DELETE CASCADE;

CREATE INDEX idx_item_photos_found_report ON item_photos(found_report_id);

ALTER TABLE item_photos DROP CONSTRAINT chk_item_photos_owner;

ALTER TABLE item_photos ADD CONSTRAINT chk_item_photos_owner CHECK (
    (lost_item_id IS NOT NULL)::int +
    (found_item_id IS NOT NULL)::int +
    (found_report_id IS NOT NULL)::int = 1
);
