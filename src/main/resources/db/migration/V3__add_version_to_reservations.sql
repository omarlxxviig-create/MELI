ALTER TABLE reservations ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0 NOT NULL;

-- Actualizar registros existentes
UPDATE reservations SET version = 0 WHERE version IS NULL;

-- Crear índice para mejorar rendimiento en verificaciones de versión
CREATE INDEX IF NOT EXISTS idx_reservations_version ON reservations(version);
