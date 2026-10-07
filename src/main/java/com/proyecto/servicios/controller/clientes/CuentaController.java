package com.proyecto.servicios.controller.clientes;

import com.proyecto.servicios.model.clientes.CuentaResponse;
import com.proyecto.servicios.model.clientes.SaldoResponse;
import com.proyecto.servicios.service.clientes.CuentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cuentas")
@Tag(name = "Cuentas", description = "Operaciones de consulta sobre cuentas bancarias")
public class CuentaController {

    private final CuentaService cuentaService;

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @GetMapping("/{numeroCuenta}")
    @Operation(summary = "Consultar cuenta por número de cuenta",
               description = "Retorna la información bancaria asociada al número de cuenta especificado.")
    public CuentaResponse buscarPorNumeroCuenta(@PathVariable String numeroCuenta) {
        return cuentaService.buscarPorNumeroCuenta(numeroCuenta);
    }

    @GetMapping("/{numeroCuenta}/saldo")
    @Operation(summary = "Consultar saldo de una cuenta",
               description = "Retorna el saldo disponible y número de la cuenta bancaria.")
    public SaldoResponse consultarSaldo(@PathVariable String numeroCuenta) {
        return cuentaService.consultarSaldo(numeroCuenta);
    }

    @GetMapping("/activas")
    @Operation(summary = "Consultar cuentas activas",
               description = "Lista todas las cuentas bancarias con estatus ACTIVA.")
    public List<CuentaResponse> listarCuentasActivas() {
        return cuentaService.listarCuentasActivas();
    }
}
