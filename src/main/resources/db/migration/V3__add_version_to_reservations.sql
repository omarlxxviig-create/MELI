ALTER TABLE reservation ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0 NOT NULL;

-- Actualizar registros existentes
UPDATE reservation SET version = 0 WHERE version IS NULL;

-- Crear índice para mejorar rendimiento en verificaciones de versión
CREATE INDEX IF NOT EXISTS idx_reservation_version ON reservation(version);
