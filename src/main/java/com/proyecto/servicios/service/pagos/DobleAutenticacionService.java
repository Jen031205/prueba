package com.proyecto.servicios.service.pagos;

import com.proyecto.servicios.entity.sf.clientes.Cliente;
import com.proyecto.servicios.model.pagos.Solicitar2FAResponse;

import java.math.BigDecimal;

public interface DobleAutenticacionService {

    Solicitar2FAResponse generarYEnviarCodigo(Cliente cliente);

    boolean validarCodigo(Long clienteId, String codigo);

    void enviarComprobantePago(Cliente cliente, String folioTransaccion, BigDecimal monto,
                               String referencia, String numeroCuenta, BigDecimal nuevoSaldo);
}
