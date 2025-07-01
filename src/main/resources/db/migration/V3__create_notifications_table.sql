-- V3__create_notifications_table.sql
-- Crear tabla de notificaciones para Long Polling

-- CREATE TABLE notifications (
--     id BIGSERIAL PRIMARY KEY,
--     title VARCHAR(255) NOT NULL,
--     message TEXT NOT NULL,
--     type VARCHAR(100) NOT NULL,
--     user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
--     route_id BIGINT REFERENCES routes(id) ON DELETE SET NULL,
--     is_read BOOLEAN DEFAULT FALSE,
--     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
--     read_at TIMESTAMP,
--     data TEXT
-- );

-- Índices para optimizar consultas de Long Polling
-- CREATE INDEX idx_notifications_user_unread ON notifications(user_id, is_read, created_at DESC);
-- CREATE INDEX idx_notifications_broadcast_unread ON notifications(user_id, is_read) WHERE user_id IS NULL;
-- CREATE INDEX idx_notifications_type ON notifications(type, is_read);
-- CREATE INDEX idx_notifications_route ON notifications(route_id);
--  CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);

-- -- Comentarios de la tabla
-- COMMENT ON TABLE notifications IS 'Tabla de notificaciones para Long
-- COMMENT ON COLUMN notifications.user_id IS 'Usuario destinatario (NULL = broadcast a todos)';
-- COMMENT ON COLUMN notifications.route_id IS 'Ruta relacionada (opcional)';
-- COMMENT ON COLUMN notifications.type IS 'Tipo: new_route_available, route_assigned, route_completed, route_cancelled, etc.';
-- COMMENT ON COLUMN notifications.data IS 'Datos adicionales en formato JSON';
-- COMMENT ON COLUMN notifications.is_read IS 'Estado de lectura de la notificación'; 