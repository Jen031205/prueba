package com.proyecto.servicios.service.usuarios;

public class UsuarioCorreoDuplicadoException extends RuntimeException {
    public UsuarioCorreoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
