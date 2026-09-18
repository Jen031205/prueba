package com.proyecto.servicios.service;

public class PuntoRedIntegrationException extends RuntimeException {

    public PuntoRedIntegrationException(String message) {
        super(message);
    }

    public PuntoRedIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
