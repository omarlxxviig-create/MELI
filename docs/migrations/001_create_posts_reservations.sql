-- Migration: 001 - Create Service Posts and Reservations Tables
-- Description: Creates the core tables for transport booking service
-- Author: System
-- Date: 2024

-- =====================================================
-- SERVICE_POST Table
-- =====================================================
CREATE TABLE IF NOT EXISTS service_post (
    id VARCHAR(36) PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    origin VARCHAR(255) NOT NULL,
    destination VARCHAR(255) NOT NULL,
    departure_date_time TIMESTAMP NOT NULL,
    seats_total INT NOT NULL CHECK (seats_total BETWEEN 1 AND 10),
    seats_available INT NOT NULL CHECK (seats_available >= 0),
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    description VARCHAR(1000),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INT NOT NULL DEFAULT 0,
    
    CONSTRAINT chk_seats_valid CHECK (seats_available <= seats_total),
    CONSTRAINT chk_status_valid CHECK (status IN ('DRAFT', 'PUBLISHED', 'CANCELLED', 'COMPLETED'))
);

-- Índices para SERVICE_POST
CREATE INDEX idx_post_owner ON service_post(owner_id);
CREATE INDEX idx_post_status ON service_post(status);
CREATE INDEX idx_post_departure ON service_post(departure_date_time);
CREATE INDEX idx_post_route ON service_post(origin, destination);

-- =====================================================
-- RESERVATION Table
-- =====================================================
CREATE TABLE IF NOT EXISTS reservation (
    id VARCHAR(36) PRIMARY KEY,
    post_id VARCHAR(36) NOT NULL,
    user_id BIGINT NOT NULL,
    seats INT NOT NULL CHECK (seats BETWEEN 1 AND 10),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    seat_allocation_details VARCHAR(1000),
    version INT NOT NULL DEFAULT 0,
    
    CONSTRAINT fk_reservation_post FOREIGN KEY (post_id) REFERENCES service_post(id) ON DELETE CASCADE,
    CONSTRAINT chk_reservation_status CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'EXPIRED'))
);

-- Índices para RESERVATION
CREATE INDEX idx_reservation_post_id ON reservation(post_id);
CREATE INDEX idx_reservation_user_id ON reservation(user_id);
CREATE INDEX idx_reservation_status ON reservation(status);
CREATE INDEX idx_reservation_expires_at ON reservation(expires_at);

-- =====================================================
-- Comments
-- =====================================================
COMMENT ON TABLE service_post IS 'Publicaciones de servicios de transporte (viajes)';
COMMENT ON TABLE reservation IS 'Reservas de asientos en publicaciones';

COMMENT ON COLUMN service_post.owner_id IS 'ID del usuario propietario (conductor)';
COMMENT ON COLUMN service_post.seats_total IS 'Capacidad total de asientos';
COMMENT ON COLUMN service_post.seats_available IS 'Asientos disponibles para reservar';
COMMENT ON COLUMN service_post.version IS 'Versión para optimistic locking';

COMMENT ON COLUMN reservation.post_id IS 'ID de la publicación reservada';
COMMENT ON COLUMN reservation.user_id IS 'ID del usuario que reserva (pasajero)';
COMMENT ON COLUMN reservation.expires_at IS 'Fecha/hora de expiración automática';
COMMENT ON COLUMN reservation.version IS 'Versión para optimistic locking';
