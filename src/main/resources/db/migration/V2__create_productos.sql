CREATE TABLE IF NOT EXISTS productos (
    id                          SERIAL PRIMARY KEY,
    servicio                    VARCHAR(100),
    producto                    VARCHAR(255),
    id_servicio                 INTEGER,
    id_producto                 INTEGER,
    id_cat_tipo_servicio        INTEGER,
    tipo_front                  VARCHAR(50),
    precio                      NUMERIC(15, 2),
    show_ayuda                  BOOLEAN,
    tipo_referencia             VARCHAR(50),
    has_digito_verificador      BOOLEAN,
    leyenda                     TEXT,
    fecha_creacion              TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_actualizacion         TIMESTAMP NOT NULL DEFAULT NOW()
);