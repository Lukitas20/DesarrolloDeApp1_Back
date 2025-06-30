-- Script para agregar la columna status a la tabla packages
ALTER TABLE packages ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'PENDING';

-- Actualizar registros existentes
UPDATE packages SET status = 'PENDING' WHERE status IS NULL; 