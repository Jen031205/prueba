package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.client.GestoPagoAuthClient;
import com.proyecto.servicios.client.PuntoRedClient;
import com.proyecto.servicios.config.PuntoRedProperties;
import com.proyecto.servicios.config.PuntoRedTokenProvider;
import com.proyecto.servicios.model.gestopago.GestoPagoAuthResponse;
import com.proyecto.servicios.service.PuntoRedIntegrationException;
import feign.FeignException;
import feign.RetryableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PuntoRedProductServiceImplTest {

    @Mock
    private PuntoRedClient puntoRedClient;

    @Mock
    private GestoPagoAuthClient gestoPagoAuthClient;

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

        @Test
        void debeObtenerTokenDeGestoPagoCuandoNoHayTokenConfigurado() {
        PuntoRedProperties properties = new PuntoRedProperties(
            "https://example.test", "", "", 5000, 10000);
        PuntoRedTokenProvider tokenProvider = new PuntoRedTokenProvider(properties);
        ReflectionTestUtils.setField(tokenProvider, "gestoPagoAuthClient", gestoPagoAuthClient);
        ReflectionTestUtils.setField(tokenProvider, "idDistribuidor", 83);
        ReflectionTestUtils.setField(tokenProvider, "codigoDispositivo", "GPS83-TPV-17");
        ReflectionTestUtils.setField(tokenProvider, "password", "12345678");
        GestoPagoAuthResponse authResponse = new GestoPagoAuthResponse();
        authResponse.setToken("token-dinamico");
        when(gestoPagoAuthClient.authenticate(83, "GPS83-TPV-17", "12345678"))
            .thenReturn(authResponse);
        service = new PuntoRedProductServiceImpl(puntoRedClient, properties, tokenProvider);
        when(puntoRedClient.getProductList("Bearer token-dinamico", ""))
            .thenReturn(ResponseEntity.ok("<RESPONSE><PRODUCTS/></RESPONSE>"));

        assertEquals("<RESPONSE><PRODUCTS/></RESPONSE>", service.getProductList().getContent());
        }
}
