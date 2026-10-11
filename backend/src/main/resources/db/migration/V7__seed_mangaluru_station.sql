-- Seed Barke (Mangaluru) police station used by the app's default city map center.
-- Idempotent: skips insert when PS-MGL-001 already exists (e.g. created via System Admin UI).

INSERT INTO locations (location_id, latitude, longitude, address_text, locality, city, state, postal_code, created_at)
SELECT '00000000-0000-0000-0000-000000000002', 12.914100, 74.856000, 'Barke', 'Barke', 'Mangaluru', 'Karnataka', '575006', now()
WHERE NOT EXISTS (
    SELECT 1 FROM police_stations WHERE station_code = 'PS-MGL-001'
);

INSERT INTO police_stations (station_id, station_name, station_code, address, phone, location_id, is_active, created_at)
SELECT '00000000-0000-0000-0000-000000000002', 'Barke Police Station', 'PS-MGL-001', 'Barke, Mangaluru, Karnataka', '08242224567',
       '00000000-0000-0000-0000-000000000002', TRUE, now()
WHERE NOT EXISTS (
    SELECT 1 FROM police_stations WHERE station_code = 'PS-MGL-001'
);
