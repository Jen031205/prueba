package com.proyecto.servicios.service.auth;

public class SesionInvalidaException extends RuntimeException {
    public SesionInvalidaException(String message) {
        super(message);
    }
}
