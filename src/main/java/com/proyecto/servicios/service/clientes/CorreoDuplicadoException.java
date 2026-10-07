package com.proyecto.servicios.service.clientes;

public class CorreoDuplicadoException extends ClienteDuplicadoException {
    public CorreoDuplicadoException(String correo) {
        super("correo electrónico: " + correo);
    }
}
