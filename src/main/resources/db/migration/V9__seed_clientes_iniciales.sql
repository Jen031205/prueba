-- Inserción de datos iniciales para pruebas (Seed Data de Clientes y Cuentas)

DO $$
DECLARE
    v_domicilio_id_1 BIGINT;
    v_cliente_id_1 BIGINT;
    v_domicilio_id_2 BIGINT;
    v_cliente_id_2 BIGINT;
BEGIN
    -- Cliente 1: Ana Maria Lopez Garcia
    IF NOT EXISTS (SELECT 1 FROM clientes WHERE curp = 'GALA900101MDFRNN09') THEN
        INSERT INTO domicilios (calle, numero_exterior, numero_interior, colonia, municipio, estado, codigo_postal, pais)
        VALUES ('Paseo de la Reforma', '222', 'Piso 8', 'Juarez', 'Cuauhtemoc', 'CDMX', '06600', 'Mexico')
        RETURNING id INTO v_domicilio_id_1;

        INSERT INTO clientes (nombre, segundo_nombre, apellido_paterno, apellido_materno, fecha_nacimiento,
                              curp, rfc, sexo, nacionalidad, estado_civil, correo_electronico,
                              telefono_movil, telefono_alternativo, ocupacion, empresa, ingreso_mensual,
                              activo, fecha_registro, domicilio_id)
        VALUES ('Ana', 'Maria', 'Lopez', 'Garcia', '1990-01-01',
                'GALA900101MDFRNN09', 'GALA900101ABC', 'Femenino', 'Mexicana', 'Soltero', 'jenniferailin031205@gmail.com',
                '5512345678', '5587654321', 'Analista Financiero', 'Banco Integrador SA', 35000.00,
                TRUE, CURRENT_TIMESTAMP, v_domicilio_id_1)
        RETURNING id INTO v_cliente_id_1;

        INSERT INTO cuentas (numero_cuenta, cliente_id, saldo, estatus, fecha_creacion)
        VALUES ('4100000000000001', v_cliente_id_1, 5000.00, 'ACTIVA', CURRENT_TIMESTAMP)
        ON CONFLICT (numero_cuenta) DO NOTHING;
    END IF;

    -- Cliente 2: Carlos Perez Diaz
    IF NOT EXISTS (SELECT 1 FROM clientes WHERE curp = 'PEDC880512HDFRNN02') THEN
        INSERT INTO domicilios (calle, numero_exterior, numero_interior, colonia, municipio, estado, codigo_postal, pais)
        VALUES ('Av Insurgentes Sur', '1602', NULL, 'Credito Constructor', 'Benito Juarez', 'CDMX', '03940', 'Mexico')
        RETURNING id INTO v_domicilio_id_2;

        INSERT INTO clientes (nombre, segundo_nombre, apellido_paterno, apellido_materno, fecha_nacimiento,
                              curp, rfc, sexo, nacionalidad, estado_civil, correo_electronico,
                              telefono_movil, telefono_alternativo, ocupacion, empresa, ingreso_mensual,
                              activo, fecha_registro, domicilio_id)
        VALUES ('Carlos', NULL, 'Perez', 'Diaz', '1988-05-12',
                'PEDC880512HDFRNN02', 'PEDC880512XYZ', 'Masculino', 'Mexicana', 'Casado', 'carlos.perez@banco.com',
                '5598765432', NULL, 'Ingeniero de Software', 'Tech Solutions SA', 52000.00,
                TRUE, CURRENT_TIMESTAMP, v_domicilio_id_2)
        RETURNING id INTO v_cliente_id_2;

        INSERT INTO cuentas (numero_cuenta, cliente_id, saldo, estatus, fecha_creacion)
        VALUES ('4100000000000002', v_cliente_id_2, 12500.00, 'ACTIVA', CURRENT_TIMESTAMP)
        ON CONFLICT (numero_cuenta) DO NOTHING;
    END IF;
END $$;
