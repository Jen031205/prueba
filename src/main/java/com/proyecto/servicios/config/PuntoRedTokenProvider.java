package com.proyecto.servicios.config;

import com.proyecto.servicios.client.GestoPagoAuthClient;
import com.proyecto.servicios.model.gestopago.GestoPagoAuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PuntoRedTokenProvider {

    private final PuntoRedProperties properties;

    @Autowired(required = false)
    private GestoPagoAuthClient gestoPagoAuthClient;

    @Value("${gestopago.auth.id-distribuidor:83}")
    private Integer idDistribuidor;

    @Value("${gestopago.auth.codigo-dispositivo:GPS83-TPV-17}")
    private String codigoDispositivo;

    @Value("${gestopago.auth.password:12345678}")
    private String password;

    private volatile String token;

    public PuntoRedTokenProvider(PuntoRedProperties properties) {
        this.properties = properties;
    }

    public synchronized String getBearerToken() {
        if (token == null || token.isBlank()) {
            token = properties.getToken();
        }

        if (token == null || token.isBlank()) {
            if (gestoPagoAuthClient == null) {
                throw new IllegalStateException("No se pudo crear el cliente de autenticacion de GestoPago");
            }
            GestoPagoAuthResponse response = gestoPagoAuthClient.authenticate(
                    idDistribuidor, codigoDispositivo, password);
            if (response == null || response.getToken() == null || response.getToken().isBlank()) {
                throw new IllegalStateException("GestoPago no devolvio un token valido");
            }
            token = response.getToken();
        }

        return "Bearer " + token;
    }
}
