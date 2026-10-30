package com.proyecto.servicios.model.pagos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Solicitar2FAResponse {

    private Boolean estado;
    private String mensaje;
    private String correoDestino;
    private Integer expiraEnMinutos;
}
