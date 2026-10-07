package com.proyecto.servicios.service.clientes;

import com.proyecto.servicios.model.clientes.CuentaResponse;
import com.proyecto.servicios.model.clientes.SaldoResponse;

import java.util.List;

public interface CuentaService {
    CuentaResponse buscarPorNumeroCuenta(String numeroCuenta);
    SaldoResponse consultarSaldo(String numeroCuenta);
    List<CuentaResponse> listarCuentasActivas();
}
