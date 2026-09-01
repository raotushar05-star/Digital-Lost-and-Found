-- Baseline item categories used across lost/found reporting and search
INSERT INTO categories (category_id, category_name, description, is_active, created_at) VALUES
    (gen_random_uuid(), 'Mobile Phone', 'Smartphones and mobile handsets', TRUE, now()),
    (gen_random_uuid(), 'Wallet', 'Wallets, purses, and card holders', TRUE, now()),
    (gen_random_uuid(), 'Bag', 'Bags, backpacks, and luggage', TRUE, now()),
    (gen_random_uuid(), 'Jewellery', 'Jewellery and precious items', TRUE, now()),
    (gen_random_uuid(), 'Documents', 'ID cards, certificates, and paperwork', TRUE, now()),
    (gen_random_uuid(), 'Electronics', 'Laptops, tablets, headphones, and gadgets', TRUE, now()),
    (gen_random_uuid(), 'Keys', 'Keys and key chains', TRUE, now()),
    (gen_random_uuid(), 'Clothing', 'Apparel and accessories', TRUE, now()),
    (gen_random_uuid(), 'Vehicle', 'Vehicles and vehicle parts', TRUE, now()),
    (gen_random_uuid(), 'Other', 'Items that do not fit another category', TRUE, now());

-- Demo location + police station so the system is usable immediately after setup
INSERT INTO locations (location_id, latitude, longitude, address_text, locality, city, state, postal_code, created_at)
VALUES ('00000000-0000-0000-0000-000000000001', 12.975500, 77.605500, 'Cubbon Park Road', 'Cubbon Park', 'Bengaluru', 'Karnataka', '560001', now());

INSERT INTO police_stations (station_id, station_name, station_code, address, phone, location_id, is_active, created_at)
VALUES ('00000000-0000-0000-0000-000000000001', 'Cubbon Park Police Station', 'PS-BLR-001', 'Kasturba Road, Bengaluru', '08022943322',
        '00000000-0000-0000-0000-000000000001', TRUE, now());
