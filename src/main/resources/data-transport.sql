-- Initial data for Transport Booking Service
-- Sample service posts and reservations for testing

-- Clean existing data (if any)
DELETE FROM reservation;
DELETE FROM service_post;

-- =====================================================
-- Sample Service Posts
-- =====================================================

-- Post 1: Zipaquirá -> Bogotá (Published)
INSERT INTO service_post (id, owner_id, origin, destination, departure_date_time, seats_total, seats_available, price, description, status, created_at, updated_at, version)
VALUES (
    'post-001',
    1,
    'Zipaquirá',
    'Bogotá',
    DATEADD('DAY', 1, CURRENT_TIMESTAMP),
    4,
    2,
    15000.00,
    'Viaje directo por autopista Norte. Salida desde el parque principal.',
    'PUBLISHED',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
);

-- Post 2: Bogotá -> Zipaquirá (Published)
INSERT INTO service_post (id, owner_id, origin, destination, departure_date_time, seats_total, seats_available, price, description, status, created_at, updated_at, version)
VALUES (
    'post-002',
    2,
    'Bogotá',
    'Zipaquirá',
    DATEADD('HOUR', 6, CURRENT_TIMESTAMP),
    5,
    5,
    18000.00,
    'Salida desde Portal del Norte. Aire acondicionado.',
    'PUBLISHED',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
);

-- Post 3: Chía -> Bogotá (Published)
INSERT INTO service_post (id, owner_id, origin, destination, departure_date_time, seats_total, seats_available, price, description, status, created_at, updated_at, version)
VALUES (
    'post-003',
    1,
    'Chía',
    'Bogotá',
    DATEADD('HOUR', 12, CURRENT_TIMESTAMP),
    3,
    3,
    12000.00,
    'Ruta por la vía a Chía. Vehículo cómodo.',
    'PUBLISHED',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
);

-- Post 4: Bogotá -> Cajicá (Draft)
INSERT INTO service_post (id, owner_id, origin, destination, departure_date_time, seats_total, seats_available, price, description, status, created_at, updated_at, version)
VALUES (
    'post-004',
    3,
    'Bogotá',
    'Cajicá',
    DATEADD('DAY', 2, CURRENT_TIMESTAMP),
    4,
    4,
    14000.00,
    'Pendiente de confirmar horario exacto.',
    'DRAFT',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
);

-- Post 5: Zipaquirá -> Bogotá (Full - no seats available)
INSERT INTO service_post (id, owner_id, origin, destination, departure_date_time, seats_total, seats_available, price, description, status, created_at, updated_at, version)
VALUES (
    'post-005',
    2,
    'Zipaquirá',
    'Bogotá',
    DATEADD('HOUR', 3, CURRENT_TIMESTAMP),
    4,
    0,
    16000.00,
    'Viaje express. Sin paradas intermedias.',
    'PUBLISHED',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
);

-- =====================================================
-- Sample Reservations
-- =====================================================

-- Reservation 1: User 10 reserves 2 seats on post-001
INSERT INTO reservation (id, post_id, user_id, seats, status, expires_at, created_at, updated_at, version)
VALUES (
    'res-001',
    'post-001',
    10,
    2,
    'CONFIRMED',
    DATEADD('HOUR', 2, CURRENT_TIMESTAMP),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
);

-- Reservation 2: User 11 reserves 4 seats on post-005 (makes it full)
INSERT INTO reservation (id, post_id, user_id, seats, status, expires_at, created_at, updated_at, version)
VALUES (
    'res-002',
    'post-005',
    11,
    4,
    'CONFIRMED',
    DATEADD('HOUR', 4, CURRENT_TIMESTAMP),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
);

-- Reservation 3: User 12 pending reservation on post-002
INSERT INTO reservation (id, post_id, user_id, seats, status, expires_at, created_at, updated_at, version)
VALUES (
    'res-003',
    'post-002',
    12,
    1,
    'PENDING',
    DATEADD('MINUTE', 15, CURRENT_TIMESTAMP),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
);

-- =====================================================
-- Verification Queries (commented out)
-- =====================================================

-- SELECT * FROM service_post WHERE status = 'PUBLISHED';
-- SELECT * FROM reservation WHERE status = 'PENDING';
-- SELECT p.*, COUNT(r.id) as total_reservations FROM service_post p LEFT JOIN reservation r ON p.id = r.post_id GROUP BY p.id;
