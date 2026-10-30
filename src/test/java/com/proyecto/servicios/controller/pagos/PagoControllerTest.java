package com.proyecto.servicios.controller.pagos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.model.pagos.DatosCompraDetalle;
import com.proyecto.servicios.model.pagos.PagoCompraRequest;
import com.proyecto.servicios.model.pagos.PagoCompraResponse;
import com.proyecto.servicios.service.pagos.PagoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PagoController.class)
class PagoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PagoService pagoService;

    @Test
    void postPagosRetorna200ConPagoConfirmado() throws Exception {
        PagoCompraRequest request = new PagoCompraRequest();
        request.setNumCuenta("4000123456789012");
        request.setIdCliente(1L);
        request.setIdProducto(101);
        request.setMonto(new BigDecimal("100.00"));
        request.setTelefono("5512345678");
        request.setReferencia("RECIBO-12345");
        request.setDobleAutentificacion("123456");

        PagoCompraResponse response = PagoCompraResponse.builder()
                .estado(true)
                .codigo("00")
                .mensaje("Transaccion Exitosa. Pago confirmado y procesado correctamente.")
                .folioTransaccion("TX-ABC123456789")
                .fechaHora("2026-10-23 18:30:00")
                .token("jwt-token-valido")
                .peticionCifrada("payloadCifradoBase64")
                .datosCompra(DatosCompraDetalle.builder()
                        .idProducto(101)
                        .idCliente(1L)
                        .telefono("5512345678")
                        .referencia("RECIBO-12345")
                        .horaLocal("2026-10-23 18:30:00")
                        .monto(new BigDecimal("100.00"))
                        .upc("1234567890")
                        .unidad("1")
                        .build())
                .numeroCuenta("4000123456789012")
                .saldoAnterior(new BigDecimal("500.00"))
                .nuevoSaldo(new BigDecimal("400.00"))
                .build();

        when(pagoService.procesarPago(any(PagoCompraRequest.class))).thenReturn(response);

        mockMvc.perform(post("/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(true))
                .andExpect(jsonPath("$.codigo").value("00"))
                .andExpect(jsonPath("$.folioTransaccion").value("TX-ABC123456789"))
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.peticionCifrada").value("payloadCifradoBase64"))
                .andExpect(jsonPath("$.datosCompra.upc").value("1234567890"))
                .andExpect(jsonPath("$.saldoAnterior").value(500.00))
                .andExpect(jsonPath("$.nuevoSaldo").value(400.00));
    }

    @Test
    void postPagosSin2FARetorna200YEnviaCodigoAlCorreo() throws Exception {
        PagoCompraRequest request = new PagoCompraRequest();
        request.setNumCuenta("4000123456789012");
        request.setIdCliente(1L);
        request.setIdProducto(101);
        request.setMonto(new BigDecimal("100.00"));
        request.setTelefono("5512345678");
        request.setReferencia("RECIBO-12345");
        // No se envía dobleAutentificacion

        PagoCompraResponse response = PagoCompraResponse.builder()
                .estado(true)
                .codigo("2FA_REQUERIDO")
                .mensaje("Código de doble autenticación enviado al correo ca***@empresa.com. Ingréselo en el campo 'dobleAutentificacion' para confirmar el pago.")
                .numeroCuenta("4000123456789012")
                .saldoAnterior(new BigDecimal("500.00"))
                .nuevoSaldo(new BigDecimal("500.00"))
                .build();

        when(pagoService.procesarPago(any(PagoCompraRequest.class))).thenReturn(response);

        mockMvc.perform(post("/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(true))
                .andExpect(jsonPath("$.codigo").value("2FA_REQUERIDO"))
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("Código de doble autenticación")));
    }

    @Test
    void postPagosRetorna400CuandoFaltanCampos() throws Exception {
        PagoCompraRequest requestIncompleto = new PagoCompraRequest();
        // Faltan campos requeridos

        mockMvc.perform(post("/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestIncompleto)))
                .andExpect(status().isBadRequest());
    }
}
