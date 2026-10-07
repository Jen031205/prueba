package com.proyecto.servicios.model.clientes.biometria;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BiometriaValidacionResponse {
    private Long clienteId;
    private boolean coincide;
    private double similitud;
    private BigDecimal porcentajeSimilitud;
    private double umbralAplicado;
    private String mensaje;
}
