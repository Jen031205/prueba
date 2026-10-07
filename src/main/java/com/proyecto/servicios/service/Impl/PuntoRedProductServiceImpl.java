package com.proyecto.servicios.service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.proyecto.servicios.client.PuntoRedClient;
import com.proyecto.servicios.entity.gestopago.Producto;
import com.proyecto.servicios.model.ProductListResponse;
import com.proyecto.servicios.repositorys.gestopago.ProductoRepository;
import com.proyecto.servicios.service.PuntoRedIntegrationException;
import com.proyecto.servicios.service.PuntoRedProductService;
import feign.FeignException;
import feign.RetryableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PuntoRedProductServiceImpl implements PuntoRedProductService {

    private static final String PRODUCT_CATALOG_KEY = "puntored:catalogo:productos";

    private final PuntoRedClient puntoRedClient;
    private final ProductoRepository productoRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public PuntoRedProductServiceImpl(PuntoRedClient puntoRedClient,
                                      ProductoRepository productoRepository,
                                      RedisTemplate<String, String> redisTemplate,
                                      ObjectMapper objectMapper) {
        this.puntoRedClient = puntoRedClient;
        this.productoRepository = productoRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public ProductListResponse getProductList() {
        log.info("Iniciando invocacion de lista de productos PuntoRed");

        try {
            ProductListResponse cachedResponse = readFromRedis();
            if (cachedResponse != null) {
                log.info("Catalogo de productos obtenido desde Redis");
                return cachedResponse;
            }

            List<Producto> storedProducts = productoRepository.findAll();
            if (!storedProducts.isEmpty()) {
                ProductListResponse databaseResponse = responseFromProducts(storedProducts);
                writeToRedis(databaseResponse);
                log.info("Catalogo de productos obtenido desde PostgreSQL");
                return databaseResponse;
            }

            ProductListResponse externalResponse = fetchFromPuntoRed();
            saveCatalog(externalResponse);
            writeToRedis(externalResponse);
            return externalResponse;
        } catch (IllegalStateException exception) {
            log.error("No fue posible obtener el token de PuntoRed", exception);
            throw new PuntoRedIntegrationException(
                "No fue posible autenticar contra PuntoRed. Configura PUNTORED_API_TOKEN o verifica las credenciales de GestoPago",
                exception);
        } catch (FeignException.Unauthorized | FeignException.Forbidden exception) {
            log.error("PuntoRed rechazo la autenticacion al consultar productos. status={}", exception.status());
            throw new PuntoRedIntegrationException(
                "PuntoRed rechazo la autenticacion (HTTP " + exception.status()
                    + "). Verifica PUNTORED_API_TOKEN y PUNTORED_API_KEY",
                exception);
        } catch (RetryableException exception) {
            log.error("Timeout o error de comunicacion al invocar lista de productos PuntoRed");
            throw new PuntoRedIntegrationException("PuntoRed no respondio dentro del tiempo esperado", exception);
        } catch (FeignException exception) {
            log.error("PuntoRed devolvio un error HTTP status={}", exception.status());
            throw new PuntoRedIntegrationException("PuntoRed devolvio una respuesta no exitosa", exception);
        } finally {
            // Cierra el flujo de logs para la operacion.
            log.info("Finalizo invocacion de lista de productos PuntoRed");
        }
    }

    @Scheduled(cron = "0 0 6 * * *", zone = "America/Mexico_City")
    @Transactional
    public void actualizarCatalogo() {
        log.info("Iniciando actualizacion programada del catalogo PuntoRed");
        try {
            ProductListResponse response = fetchFromPuntoRed();
            saveCatalog(response);
            writeToRedis(response);
            log.info("Catalogo PuntoRed actualizado correctamente");
        } catch (PuntoRedIntegrationException exception) {
            log.error("No fue posible actualizar el catalogo PuntoRed", exception);
        }
    }

    private ProductListResponse fetchFromPuntoRed() {
        try {
            ResponseEntity<String> response = puntoRedClient.getProductList();
            String content = response.getBody();
            if (!response.getStatusCode().is2xxSuccessful() || content == null) {
                throw new PuntoRedIntegrationException("PuntoRed devolvio una respuesta no exitosa");
            }
            return new ProductListResponse(content, response.getHeaders().getFirst("Content-Type"));
        } catch (FeignException.Unauthorized | FeignException.Forbidden exception) {
            throw new PuntoRedIntegrationException(
                    "PuntoRed rechazo la autenticacion (HTTP " + exception.status() + ")", exception);
        } catch (RetryableException exception) {
            throw new PuntoRedIntegrationException("PuntoRed no respondio dentro del tiempo esperado", exception);
        } catch (FeignException exception) {
            throw new PuntoRedIntegrationException("PuntoRed devolvio una respuesta no exitosa", exception);
        }
    }

    private ProductListResponse readFromRedis() {
        try {
            String cachedValue = redisTemplate.opsForValue().get(PRODUCT_CATALOG_KEY);
            if (cachedValue == null || cachedValue.isBlank()) {
                return null;
            }
            JsonNode cachedResponse = objectMapper.readTree(cachedValue);
            return new ProductListResponse(
                    cachedResponse.path("content"),
                    cachedResponse.path("contentType").asText("application/json"));
        } catch (Exception exception) {
            log.warn("Redis no disponible; se intentara leer el catalogo desde PostgreSQL");
            return null;
        }
    }

    private void writeToRedis(ProductListResponse response) {
        try {
            redisTemplate.opsForValue().set(PRODUCT_CATALOG_KEY, objectMapper.writeValueAsString(response));
        } catch (Exception exception) {
            log.warn("No fue posible guardar el catalogo en Redis");
        }
    }

    private void saveCatalog(ProductListResponse response) {
        JsonNode productsNode = response.getContent().path("RESPONSE").path("PRODUCTOS").path("producto");
        List<Producto> products = new ArrayList<>();
        if (productsNode.isArray()) {
            productsNode.forEach(productNode -> products.add(toEntity(productNode)));
        } else if (!productsNode.isMissingNode() && !productsNode.isNull()) {
            products.add(toEntity(productsNode));
        }
        productoRepository.deleteAllInBatch();
        productoRepository.saveAll(products);
    }

    private Producto toEntity(JsonNode node) {
        Producto product = new Producto();
        product.setServicio(text(node, "servicio"));
        product.setProducto(text(node, "producto"));
        product.setIdServicio(integer(node, "idServicio"));
        product.setIdProducto(integer(node, "idProducto"));
        product.setIdCatTipoServicio(integer(node, "idCatTipoServicio"));
        product.setTipoFront(text(node, "tipoFront"));
        product.setPrecio(decimal(node, "precio"));
        product.setShowAyuda(booleanValue(node, "showAyuda"));
        product.setTipoReferencia(text(node, "tipoReferencia"));
        product.setHasDigitoVerificador(booleanValue(node, "hasDigitoVerificador"));
        product.setLeyenda(text(node, "leyenda"));
        return product;
    }

    private ProductListResponse responseFromProducts(List<Producto> products) {
        ObjectNode responseNode = objectMapper.createObjectNode();
        ObjectNode response = responseNode.putObject("RESPONSE");
        ArrayNode productArray = response.putObject("PRODUCTOS").putArray("producto");
        products.forEach(product -> {
            ObjectNode node = productArray.addObject();
            node.put("servicio", product.getServicio());
            node.put("producto", product.getProducto());
            node.put("idServicio", product.getIdServicio());
            node.put("idProducto", product.getIdProducto());
            node.put("idCatTipoServicio", product.getIdCatTipoServicio());
            node.put("tipoFront", product.getTipoFront());
            node.put("precio", product.getPrecio());
            node.put("showAyuda", product.getShowAyuda());
            node.put("tipoReferencia", product.getTipoReferencia());
            node.put("hasDigitoVerificador", product.getHasDigitoVerificador());
            node.put("leyenda", product.getLeyenda());
        });
        return new ProductListResponse(responseNode, "application/json");
    }

    private String text(JsonNode node, String field) {
        return node.path(field).isMissingNode() ? null : node.path(field).asText();
    }

    private Integer integer(JsonNode node, String field) {
        return node.path(field).isNumber() ? node.path(field).asInt() : null;
    }

    private BigDecimal decimal(JsonNode node, String field) {
        return node.path(field).isNumber() ? node.path(field).decimalValue() : null;
    }

    private Boolean booleanValue(JsonNode node, String field) {
        return node.path(field).isBoolean() ? node.path(field).asBoolean() : null;
    }
}
