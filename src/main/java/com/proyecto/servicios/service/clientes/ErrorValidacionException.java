package com.proyecto.servicios.service.clientes;

public class ErrorValidacionException extends RuntimeException {
    public ErrorValidacionException(String mensaje) {
        super(mensaje);
    }
}
