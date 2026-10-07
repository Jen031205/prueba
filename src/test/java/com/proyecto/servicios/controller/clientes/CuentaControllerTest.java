package com.proyecto.servicios.controller.clientes;

import com.proyecto.servicios.model.clientes.CuentaResponse;
import com.proyecto.servicios.model.clientes.SaldoResponse;
import com.proyecto.servicios.service.clientes.CuentaNoEncontradaException;
import com.proyecto.servicios.service.clientes.CuentaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CuentaController.class)
class CuentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CuentaService cuentaService;

    @Test
    void getCuentaPorNumeroRetornaCuenta() throws Exception {
        CuentaResponse response = new CuentaResponse();
        response.setNumeroCuenta("4100000000000001");
        response.setSaldo(new BigDecimal("100.00"));
        response.setEstatus("ACTIVA");

        when(cuentaService.buscarPorNumeroCuenta("4100000000000001")).thenReturn(response);

        mockMvc.perform(get("/cuentas/4100000000000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroCuenta").value("4100000000000001"))
                .andExpect(jsonPath("$.saldo").value(100.00))
                .andExpect(jsonPath("$.estatus").value("ACTIVA"));
    }

    @Test
    void getCuentaPorNumeroRetorna404CuandoNoExiste() throws Exception {
        when(cuentaService.buscarPorNumeroCuenta("9999999999999999"))
                .thenThrow(new CuentaNoEncontradaException("9999999999999999"));

        mockMvc.perform(get("/cuentas/9999999999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getSaldoRetornaSaldoResponse() throws Exception {
        SaldoResponse saldo = new SaldoResponse("4100000000000001", new BigDecimal("250.75"));

        when(cuentaService.consultarSaldo("4100000000000001")).thenReturn(saldo);

        mockMvc.perform(get("/cuentas/4100000000000001/saldo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroCuenta").value("4100000000000001"))
                .andExpect(jsonPath("$.saldo").value(250.75));
    }

    @Test
    void getCuentasActivasRetornaLista() throws Exception {
        CuentaResponse c = new CuentaResponse();
        c.setNumeroCuenta("4100000000000001");
        c.setEstatus("ACTIVA");

        when(cuentaService.listarCuentasActivas()).thenReturn(List.of(c));

        mockMvc.perform(get("/cuentas/activas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numeroCuenta").value("4100000000000001"));
    }
}
