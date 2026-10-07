package com.proyecto.servicios.service.clientes;

public class CurpDuplicadaException extends ClienteDuplicadoException {
    public CurpDuplicadaException(String curp) {
        super("CURP: " + curp);
    }
}
