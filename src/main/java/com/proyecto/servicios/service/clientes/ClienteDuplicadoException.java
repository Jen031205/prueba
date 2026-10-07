package com.proyecto.servicios.service.clientes;

public class ClienteDuplicadoException extends ClienteYaRegistradoException {
    public ClienteDuplicadoException(String campo) {
        super("Ya existe un cliente registrado con ese " + campo);
    }
}