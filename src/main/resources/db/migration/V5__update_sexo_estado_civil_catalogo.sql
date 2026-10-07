-- Actualización de catálogo de sexo para permitir texto tal cual ('Masculino', 'Femenino', 'Otro')
ALTER TABLE clientes ALTER COLUMN sexo TYPE VARCHAR(20);

ALTER TABLE clientes DROP CONSTRAINT IF EXISTS clientes_sexo_check;

-- Homologar registros previos
UPDATE clientes SET sexo = 'Masculino' WHERE sexo = 'H';
UPDATE clientes SET sexo = 'Femenino' WHERE sexo = 'M';
UPDATE clientes SET sexo = 'Otro' WHERE sexo NOT IN ('Masculino', 'Femenino');

ALTER TABLE clientes ADD CONSTRAINT clientes_sexo_check 
    CHECK (sexo IN ('Masculino', 'Femenino', 'Otro'));

-- Actualización de catálogo de estado_civil para eliminar el caracter especial guion bajo ('UNION LIBRE')
ALTER TABLE clientes DROP CONSTRAINT IF EXISTS clientes_estado_civil_check;

UPDATE clientes SET estado_civil = 'UNION LIBRE' WHERE estado_civil = 'UNION_LIBRE';

ALTER TABLE clientes ADD CONSTRAINT clientes_estado_civil_check 
    CHECK (estado_civil IN ('SOLTERO', 'CASADO', 'DIVORCIADO', 'VIUDO', 'UNION LIBRE'));
