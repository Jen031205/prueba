# Integracion PuntoRed

## Alcance

Se incorporo el consumo de `GET /sistema/service/getProductList.do` mediante Feign.
El endpoint interno expuesto por la aplicacion es `GET /api/v1/productos`.

## Configuracion

Las propiedades se encuentran en `src/main/resources/application.properties` y admiten variables de entorno:

- `PUNTORED_API_BASE_URL`: URL base de PuntoRed.
- `PUNTORED_API_TOKEN`: token Bearer. No debe almacenarse en el codigo fuente.
- `PUNTORED_API_KEY`: API key opcional entregada por PuntoRed.
- `PUNTORED_API_CONNECT_TIMEOUT_MS`: timeout de conexion.
- `PUNTORED_API_READ_TIMEOUT_MS`: timeout de lectura.
- `REDIS_HOST`: host de Redis, por defecto `localhost`.
- `REDIS_PORT`: puerto de Redis, por defecto `6379`.
- `REDIS_PASSWORD`: password opcional de Redis.

El token se envia como `Authorization: Bearer <token>`. La API key se envia como `X-API-Key` cuando esta configurada.

## Arquitectura

- `PuntoRedClient`: cliente Feign para la API externa.
- `PuntoRedProductService`: contrato de negocio.
- `PuntoRedProductServiceImpl`: agrega autenticacion, logs y traduccion de errores.
- `ProductListResponse`: DTO que conserva la respuesta externa.
- `PuntoRedProductController`: endpoint interno `/productos`.

El catalogo se persiste en PostgreSQL mediante `ProductoRepository` y la tabla
`productos` creada por Flyway. Redis usa la clave `puntored:catalogo:productos`;
la lectura intenta primero Redis, luego PostgreSQL, y el refresco programado de
las 06:00 actualiza ambas fuentes.

La documentacion de PuntoRed muestra respuestas XML, pero no se recibio un esquema completo de productos. Por eso la respuesta se conserva como texto XML en el DTO, evitando inventar campos y permitiendo tiparla cuando el proveedor entregue el contrato definitivo.

## Errores

Se traducen errores de autenticacion (`401` y `403`), errores HTTP, timeouts y fallos de comunicacion a `PuntoRedIntegrationException`. Los logs registran inicio, finalizacion y tipo de error sin escribir tokens, passwords ni API keys.

## Pruebas

`PuntoRedProductServiceImplTest` simula:

- Respuesta exitosa.
- Error de autenticacion.
- Timeout o error de comunicacion.
- Respuesta HTTP no exitosa.

Las pruebas no realizan llamadas reales a PuntoRed.
