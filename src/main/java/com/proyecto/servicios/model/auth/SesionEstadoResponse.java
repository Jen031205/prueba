package com.proyecto.servicios.model.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SesionEstadoResponse {
    private boolean activa;
    private String correo;
    private LocalDateTime fechaInicio;
    private LocalDateTime ultimaActividad;
    private LocalDateTime fechaExpiracion;
    private long segundosRestantes;
    private String alerta;
    private String mensaje;
}
