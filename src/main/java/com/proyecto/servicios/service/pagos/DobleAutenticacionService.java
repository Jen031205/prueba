package com.proyecto.servicios.service.pagos;

import com.proyecto.servicios.entity.sf.clientes.Cliente;
import com.proyecto.servicios.model.pagos.Solicitar2FAResponse;

public interface DobleAutenticacionService {

    Solicitar2FAResponse generarYEnviarCodigo(Cliente cliente);

    boolean validarCodigo(Long clienteId, String codigo);
}
