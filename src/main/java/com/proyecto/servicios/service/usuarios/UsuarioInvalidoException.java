package com.proyecto.servicios.service.usuarios;

public class UsuarioInvalidoException extends RuntimeException {
    public UsuarioInvalidoException(String mensaje) {
        super(mensaje);
    }
}
