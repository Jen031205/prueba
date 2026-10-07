package com.proyecto.servicios.service.clientes;

import com.proyecto.servicios.entity.sf.clientes.Cliente;
import com.proyecto.servicios.entity.sf.clientes.Cuenta;
import com.proyecto.servicios.model.clientes.CuentaResponse;
import com.proyecto.servicios.model.clientes.SaldoResponse;
import com.proyecto.servicios.repositorys.sf.clientes.CuentaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CuentaServiceImplTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @InjectMocks
    private CuentaServiceImpl cuentaService;

    @Test
    void buscarPorNumeroCuentaRetornaCuentaCuandoExiste() {
        Cliente cliente = new Cliente();
        cliente.setId(10L);

        Cuenta cuenta = new Cuenta();
        cuenta.setId(1L);
        cuenta.setNumeroCuenta("4100000000000001");
        cuenta.setSaldo(new BigDecimal("1500.50"));
        cuenta.setEstatus("ACTIVA");
        cuenta.setFechaCreacion(LocalDateTime.now());
        cuenta.setCliente(cliente);

        when(cuentaRepository.findByNumeroCuenta("4100000000000001")).thenReturn(Optional.of(cuenta));

        CuentaResponse response = cuentaService.buscarPorNumeroCuenta("4100000000000001");
        assertNotNull(response);
        assertEquals("4100000000000001", response.getNumeroCuenta());
        assertEquals(new BigDecimal("1500.50"), response.getSaldo());
        assertEquals("ACTIVA", response.getEstatus());
        assertEquals(10L, response.getClienteId());
    }

    @Test
    void buscarPorNumeroCuentaLanzaExcepcionCuandoNoExiste() {
        when(cuentaRepository.findByNumeroCuenta("9999999999999999")).thenReturn(Optional.empty());

        assertThrows(CuentaNoEncontradaException.class,
                () -> cuentaService.buscarPorNumeroCuenta("9999999999999999"));
    }

    @Test
    void consultarSaldoRetornaSaldoCorrecto() {
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta("4100000000000002");
        cuenta.setSaldo(new BigDecimal("500.00"));

        when(cuentaRepository.findByNumeroCuenta("4100000000000002")).thenReturn(Optional.of(cuenta));

        SaldoResponse response = cuentaService.consultarSaldo("4100000000000002");
        assertNotNull(response);
        assertEquals("4100000000000002", response.getNumeroCuenta());
        assertEquals(new BigDecimal("500.00"), response.getSaldo());
    }

    @Test
    void listarCuentasActivasRetornaLista() {
        Cuenta c1 = new Cuenta();
        c1.setNumeroCuenta("4100000000000001");
        c1.setEstatus("ACTIVA");
        c1.setSaldo(BigDecimal.ZERO);

        Cuenta c2 = new Cuenta();
        c2.setNumeroCuenta("4100000000000002");
        c2.setEstatus("ACTIVA");
        c2.setSaldo(new BigDecimal("100.00"));

        when(cuentaRepository.findByEstatus("ACTIVA")).thenReturn(List.of(c1, c2));

        List<CuentaResponse> activas = cuentaService.listarCuentasActivas();
        assertEquals(2, activas.size());
    }
}
