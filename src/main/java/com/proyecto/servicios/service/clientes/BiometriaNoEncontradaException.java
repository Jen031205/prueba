package com.proyecto.servicios.service.clientes;

public class BiometriaNoEncontradaException extends RuntimeException {
    public BiometriaNoEncontradaException(String detalle) {
        super("No se encontraron datos biométricos registrados para el cliente con " + detalle);
    }
}
