package com.proyecto.servicios.service.clientes;

public class ClienteNoEncontradoException extends RuntimeException {
    public ClienteNoEncontradoException(String identificador) {
        super("No se encontró el cliente: " + identificador);
    }
}