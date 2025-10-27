-- Índice compuesto para búsquedas rápidas de inventario
CREATE INDEX IF NOT EXISTS idx_store_product ON inventories(store_id, product_id);

-- Índice para reservas por estado
CREATE INDEX IF NOT EXISTS idx_reservations_status ON reservations(status);

-- Índice para reservas por fecha de expiración
CREATE INDEX IF NOT EXISTS idx_reservations_expires_at ON reservations(expires_at);

-- Índice compuesto para búsquedas de reservas activas
CREATE INDEX IF NOT EXISTS idx_reservations_active ON reservations(store_id, product_id, status);

-- Índice para usuarios por username (si no existe)
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);

-- Índice para usuarios por email (si no existe)
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
