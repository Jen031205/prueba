package com.proyecto.servicios.service.clientes;

import com.proyecto.servicios.entity.sf.clientes.Cuenta;
import com.proyecto.servicios.model.clientes.CuentaResponse;
import com.proyecto.servicios.model.clientes.SaldoResponse;
import com.proyecto.servicios.repositorys.sf.clientes.CuentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CuentaServiceImpl implements CuentaService {

    private static final String CUENTA_ACTIVA = "ACTIVA";
    private final CuentaRepository cuentaRepository;

    public CuentaServiceImpl(CuentaRepository cuentaRepository) {
        this.cuentaRepository = cuentaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public CuentaResponse buscarPorNumeroCuenta(String numeroCuenta) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new CuentaNoEncontradaException(numeroCuenta));
        return toCuentaResponse(cuenta);
    }

    @Override
    @Transactional(readOnly = true)
    public SaldoResponse consultarSaldo(String numeroCuenta) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new CuentaNoEncontradaException(numeroCuenta));
        return new SaldoResponse(cuenta.getNumeroCuenta(), cuenta.getSaldo());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaResponse> listarCuentasActivas() {
        return cuentaRepository.findByEstatus(CUENTA_ACTIVA).stream()
                .map(this::toCuentaResponse)
                .toList();
    }

    private CuentaResponse toCuentaResponse(Cuenta cuenta) {
        CuentaResponse respuesta = new CuentaResponse();
        respuesta.setNumeroCuenta(cuenta.getNumeroCuenta());
        respuesta.setClienteId(cuenta.getCliente() != null ? cuenta.getCliente().getId() : null);
        respuesta.setSaldo(cuenta.getSaldo());
        respuesta.setEstatus(cuenta.getEstatus());
        respuesta.setFechaCreacion(cuenta.getFechaCreacion());
        return respuesta;
    }
}
