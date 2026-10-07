package com.proyecto.servicios.model.clientes.biometria;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class BiometriaResponse {
    private Long id;
    private Long clienteId;
    private String tipoBiometria;
    private BigDecimal confianzaDeteccion;
    private Integer rostrosDetectados;
    private LocalDateTime fechaCaptura;
    private String estatus;
    private boolean tieneFoto;
    private int dimensionVector;
}
