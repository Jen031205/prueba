-- Actualización de catálogo de estado_civil a formato capitalizado (Primera mayúscula y luego minúsculas)
ALTER TABLE clientes DROP CONSTRAINT IF EXISTS clientes_estado_civil_check;

UPDATE clientes SET estado_civil = 'Soltero' WHERE UPPER(estado_civil) = 'SOLTERO';
UPDATE clientes SET estado_civil = 'Casado' WHERE UPPER(estado_civil) = 'CASADO';
UPDATE clientes SET estado_civil = 'Divorciado' WHERE UPPER(estado_civil) = 'DIVORCIADO';
UPDATE clientes SET estado_civil = 'Viudo' WHERE UPPER(estado_civil) = 'VIUDO';
UPDATE clientes SET estado_civil = 'Union libre' WHERE UPPER(estado_civil) IN ('UNION LIBRE', 'UNION_LIBRE');

ALTER TABLE clientes ADD CONSTRAINT clientes_estado_civil_check 
    CHECK (estado_civil IN ('Soltero', 'Casado', 'Divorciado', 'Viudo', 'Union libre', 'Union Libre'));
