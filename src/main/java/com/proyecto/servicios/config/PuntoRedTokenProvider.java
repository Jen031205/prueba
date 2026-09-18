package com.proyecto.servicios.config;

import org.springframework.stereotype.Component;

@Component
public class PuntoRedTokenProvider {

    private final PuntoRedProperties properties;

    public PuntoRedTokenProvider(PuntoRedProperties properties) {
        this.properties = properties;
    }

    public String getBearerToken() {
        // Valida que el token exista antes de enviarlo al servicio externo.
        if (properties.getToken() == null || properties.getToken().isBlank()) {
            throw new IllegalStateException("PUNTORED_API_TOKEN no esta configurado");
        }
        return "Bearer " + properties.getToken();
    }
}
