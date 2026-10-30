package com.proyecto.servicios.model.pagos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoCompraResponse {

    private Boolean estado;
    private String codigo;
    private String mensaje;
    private String folioTransaccion;
    private String fechaHora;

    // Seguridad: Se excluye de la serialización JSON HTTP para proteger credenciales
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String token;
    private String peticionCifrada;
    private DatosCompraDetalle datosCompra;
    private String numeroCuenta;
    private BigDecimal saldoAnterior;
    private BigDecimal nuevoSaldo;
}
