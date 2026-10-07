package com.proyecto.servicios.service.clientes;

public class CuentaNoEncontradaException extends RuntimeException {
    public CuentaNoEncontradaException(String numeroCuenta) {
        super("No se encontró la cuenta: " + numeroCuenta);
    }
}