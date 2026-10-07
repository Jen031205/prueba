package com.proyecto.servicios.service.clientes;

public class RfcDuplicadoException extends ClienteDuplicadoException {
    public RfcDuplicadoException(String rfc) {
        super("RFC: " + rfc);
    }
}
