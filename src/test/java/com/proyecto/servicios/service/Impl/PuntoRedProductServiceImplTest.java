package com.proyecto.servicios.service.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.client.PuntoRedClient;
import com.proyecto.servicios.entity.gestopago.Producto;
import com.proyecto.servicios.model.ProductListResponse;
import com.proyecto.servicios.repositorys.gestopago.ProductoRepository;
import com.proyecto.servicios.service.PuntoRedIntegrationException;
import feign.FeignException;
import feign.RetryableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PuntoRedProductServiceImplTest {

    @Mock
    private PuntoRedClient puntoRedClient;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private PuntoRedProductServiceImpl service;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(valueOperations.get("puntored:catalogo:productos")).thenReturn(null);
        lenient().when(productoRepository.findAll()).thenReturn(Collections.emptyList());
        service = new PuntoRedProductServiceImpl(
            puntoRedClient, productoRepository, redisTemplate, new ObjectMapper());
    }

    @Test
    void debeRetornarLaRespuestaCuandoLaInvocacionEsExitosa() {
        when(puntoRedClient.getProductList())
                .thenReturn(ResponseEntity.ok("<RESPONSE><PRODUCTS/></RESPONSE>"));

        String content = service.getProductList().getContent().toString();
        assertTrue(content.contains("RESPONSE"));
        assertTrue(content.contains("PRODUCTS"));
    }

    @Test
    void debeTraducirErrorDeAutenticacion() {
        when(puntoRedClient.getProductList())
                .thenThrow(mock(FeignException.Unauthorized.class));

        assertThrows(PuntoRedIntegrationException.class, () -> service.getProductList());
    }

    @Test
    void debeTraducirTimeout() {
        when(puntoRedClient.getProductList())
                .thenThrow(mock(RetryableException.class));

        assertThrows(PuntoRedIntegrationException.class, () -> service.getProductList());
    }

    @Test
    void debeTraducirErrorHttp() {
        when(puntoRedClient.getProductList())
                .thenThrow(mock(FeignException.class));

        assertThrows(PuntoRedIntegrationException.class, () -> service.getProductList());
    }

    @Test
    void debeRetornarElCatalogoDesdeRedis() throws Exception {
        String cachedResponse = new ObjectMapper().writeValueAsString(
                new ProductListResponse(new ObjectMapper().readTree("{\"RESPONSE\":{\"PRODUCTOS\":{\"producto\":[]}}}"), "application/json"));
        when(valueOperations.get("puntored:catalogo:productos")).thenReturn(cachedResponse);

        assertTrue(service.getProductList().getContent().has("RESPONSE"));
        verify(puntoRedClient, never()).getProductList();
        verify(productoRepository, never()).findAll();
    }

    @Test
    void debeUsarPostgreSqlCuandoRedisNoEstaDisponible() {
        Producto product = new Producto();
        product.setServicio("ABIB");
        product.setProducto("ABIB 130");
        when(valueOperations.get("puntored:catalogo:productos"))
                .thenThrow(new IllegalStateException("Redis apagado"));
        when(productoRepository.findAll()).thenReturn(Collections.singletonList(product));

        assertTrue(service.getProductList().getContent().has("RESPONSE"));
        verify(puntoRedClient, never()).getProductList();
        verify(valueOperations).set(org.mockito.ArgumentMatchers.eq("puntored:catalogo:productos"),
                org.mockito.ArgumentMatchers.anyString());
    }

        @Test
        void debeActualizarPostgreSqlYRedisEnLaActualizacionProgramada() {
        when(puntoRedClient.getProductList())
            .thenReturn(ResponseEntity.ok("<RESPONSE><PRODUCTOS><producto>"
                + "<servicio>ABIB</servicio><producto>ABIB 130</producto>"
                + "<idServicio>1</idServicio><idProducto>2</idProducto>"
                + "</producto></PRODUCTOS></RESPONSE>"));

        service.actualizarCatalogo();

        verify(productoRepository).deleteAllInBatch();
        verify(productoRepository).saveAll(org.mockito.ArgumentMatchers.argThat(products ->
            products instanceof List<?> && ((List<?>) products).size() == 1));
        verify(valueOperations).set(org.mockito.ArgumentMatchers.eq("puntored:catalogo:productos"),
            org.mockito.ArgumentMatchers.anyString());
        }
}
