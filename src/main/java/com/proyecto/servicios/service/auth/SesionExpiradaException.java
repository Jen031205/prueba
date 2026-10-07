package com.proyecto.servicios.service.auth;

public class SesionExpiradaException extends RuntimeException {
    public SesionExpiradaException(String message) {
        super(message);
    }
}
