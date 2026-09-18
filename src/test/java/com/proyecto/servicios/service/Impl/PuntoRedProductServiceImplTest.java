package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.client.PuntoRedClient;
import com.proyecto.servicios.config.PuntoRedProperties;
import com.proyecto.servicios.config.PuntoRedTokenProvider;
import com.proyecto.servicios.service.PuntoRedIntegrationException;
import feign.FeignException;
import feign.RetryableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PuntoRedProductServiceImplTest {

    @Mock
    private PuntoRedClient puntoRedClient;

    private PuntoRedProductServiceImpl service;

    @BeforeEach
    void setUp() {
        PuntoRedProperties properties = new PuntoRedProperties(
                "https://example.test", "token-test", "", 5000, 10000);
        PuntoRedTokenProvider tokenProvider = new PuntoRedTokenProvider(properties);
        service = new PuntoRedProductServiceImpl(puntoRedClient, properties, tokenProvider);
    }

    @Test
    void debeRetornarLaRespuestaCuandoLaInvocacionEsExitosa() {
        when(puntoRedClient.getProductList("Bearer token-test", ""))
                .thenReturn(ResponseEntity.ok("<RESPONSE><PRODUCTS/></RESPONSE>"));

        assertEquals("<RESPONSE><PRODUCTS/></RESPONSE>", service.getProductList().getContent());
    }

    @Test
    void debeTraducirErrorDeAutenticacion() {
        when(puntoRedClient.getProductList("Bearer token-test", ""))
                .thenThrow(mock(FeignException.Unauthorized.class));

        assertThrows(PuntoRedIntegrationException.class, () -> service.getProductList());
    }

    @Test
    void debeTraducirTimeout() {
        when(puntoRedClient.getProductList("Bearer token-test", ""))
                .thenThrow(mock(RetryableException.class));

        assertThrows(PuntoRedIntegrationException.class, () -> service.getProductList());
    }

    @Test
    void debeTraducirErrorHttp() {
        when(puntoRedClient.getProductList("Bearer token-test", ""))
                .thenThrow(mock(FeignException.class));

        assertThrows(PuntoRedIntegrationException.class, () -> service.getProductList());
    }
}
