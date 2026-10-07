-- Índices de alto rendimiento para soportar concurrencia y pruebas de estrés (JMeter)
CREATE INDEX IF NOT EXISTS idx_sesiones_usuario_id_activa ON sesiones_usuario (usuario_id, activa);
CREATE INDEX IF NOT EXISTS idx_cuentas_cliente_estatus ON cuentas (cliente_id, estatus);
